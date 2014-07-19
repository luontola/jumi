// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

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

@NotThreadSafe
public class IpcCommandSender implements CommandListener, Closeable {

    private final IpcWriter<RequestListener> requestWriter;
    private final RequestListener request;
    private final IpcReader<ResponseListener> responseReader;

    public IpcCommandSender(CommandDir dir) {
        requestWriter = IpcChannel.writer(dir.getRequestPath(), RequestListenerEncoding::new);
        request = new RequestListenerEventizer().newFrontend(requestWriter);
        responseReader = IpcChannel.reader(dir.getResponsePath(), ResponseListenerEncoding::new);
    }

    @Override
    public void close() {
        requestWriter.close();
    }

    @Override
    public void runTests(SuiteConfiguration suiteConfiguration, SuiteListener suiteListener) {
        this.tmpSuiteListener = suiteListener;
        request.runTests(suiteConfiguration);
        // TODO: read response (async)
    }


    private SuiteListener tmpSuiteListener; // XXX

    public void poll_UGLY_HACK() throws InterruptedException { // XXX
        // TODO: this should be asynchronous
        IpcReaders.decodeAll(responseReader, new ResponseListener() {
            @Override
            public void onSuiteStarted(Path suiteResults) {
                // XXX
                IpcReader<SuiteListener> suiteReader = IpcChannel.reader(suiteResults, SuiteListenerEncoding::new);
                try {
                    IpcReaders.decodeAll(suiteReader, tmpSuiteListener);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    @Override
    public void shutdown() {
        request.shutdown();
    }
}
