// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import com.google.common.util.concurrent.SettableFuture;
import fi.jumi.core.api.SuiteListener;
import fi.jumi.core.config.SuiteConfiguration;
import fi.jumi.core.events.RequestListenerEventizer;
import fi.jumi.core.ipc.api.*;
import fi.jumi.core.ipc.channel.*;
import fi.jumi.core.ipc.dirs.CommandDir;
import fi.jumi.core.ipc.encoding.*;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.Closeable;
import java.nio.file.Path;
import java.util.concurrent.*;

@NotThreadSafe
public class IpcCommandSender implements Closeable {

    private final IpcWriter<RequestListener> requestWriter;
    private final RequestListener requestSender;
    private final BlockingQueue<ResponseListener> handlersForExpectedResponses = new LinkedBlockingQueue<>();

    public IpcCommandSender(CommandDir commandDir, Executor workerThreads) {
        this.requestWriter = IpcChannel.writer(commandDir.getRequestPath(), RequestListenerEncoding::new);
        this.requestSender = new RequestListenerEventizer().newFrontend(requestWriter);
        workerThreads.execute(new ResponseReader(commandDir, handlersForExpectedResponses));
    }

    @Override
    public void close() {
        requestWriter.close();
    }

    public Future<IpcReader<SuiteListener>> runTests(SuiteConfiguration suiteConfiguration) {
        SettableFuture<IpcReader<SuiteListener>> future = SettableFuture.create();
        handlersForExpectedResponses.add(new ResponseListener() {
            @Override
            public void onSuiteStarted(Path suiteResults) {
                future.set(IpcChannel.reader(suiteResults, SuiteListenerEncoding::new));
            }
        });
        requestSender.runTests(suiteConfiguration);
        return future;
    }

    public void shutdown() {
        requestSender.shutdown();
    }


    @NotThreadSafe
    private static class ResponseReader implements Runnable {
        private final CommandDir commandDir;
        private final BlockingQueue<ResponseListener> handlersForExpectedResponses;

        public ResponseReader(CommandDir commandDir, BlockingQueue<ResponseListener> handlersForExpectedResponses) {
            this.commandDir = commandDir;
            this.handlersForExpectedResponses = handlersForExpectedResponses;
        }

        @Override
        public void run() {
            IpcReader<ResponseListener> responseReader = IpcChannel.reader(commandDir.getResponsePath(), ResponseListenerEncoding::new);
            ResponseListener responseHandler = new ResponseListener() {
                @Override
                public void onSuiteStarted(Path suiteResults) {
                    ResponseListener handler = handlersForExpectedResponses.poll();
                    if (handler == null) {
                        throw new IllegalStateException("Nobody was expecting this event");
                    }
                    handler.onSuiteStarted(suiteResults);
                }
            };
            try {
                IpcReaders.decodeAll(responseReader, responseHandler);
            } catch (InterruptedException e) {
                System.err.println(this + " interrupted");
                Thread.currentThread().interrupt();
            }
        }
    }
}
