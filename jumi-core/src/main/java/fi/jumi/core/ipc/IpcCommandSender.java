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
    private final RequestListener request;
    private final IpcReader<ResponseListener> responseReader;
    private final BlockingQueue<ResponseListener> handlersForExpectedResponses = new LinkedBlockingQueue<>();

    public IpcCommandSender(CommandDir dir) {
        requestWriter = IpcChannel.writer(dir.getRequestPath(), RequestListenerEncoding::new);
        request = new RequestListenerEventizer().newFrontend(requestWriter);
        responseReader = IpcChannel.reader(dir.getResponsePath(), ResponseListenerEncoding::new);
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
        request.runTests(suiteConfiguration);
        return future;
    }

    public void readResponses() throws InterruptedException {
        IpcReaders.decodeAll(responseReader, new ResponseListener() {
            @Override
            public void onSuiteStarted(Path suiteResults) {
                ResponseListener handler = handlersForExpectedResponses.poll();
                if (handler == null) {
                    throw new IllegalStateException("Nobody was expecting this event");
                }
                handler.onSuiteStarted(suiteResults);
            }
        });
    }

    public void shutdown() {
        request.shutdown();
    }
}
