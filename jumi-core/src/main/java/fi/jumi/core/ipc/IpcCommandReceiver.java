// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import fi.jumi.core.api.*;
import fi.jumi.core.config.SuiteConfiguration;
import fi.jumi.core.ipc.api.*;
import fi.jumi.core.ipc.channel.*;
import fi.jumi.core.ipc.dirs.CommandDir;
import fi.jumi.core.ipc.encoding.RequestListenerEncoding;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class IpcCommandReceiver {

    private final IpcReader<RequestListener> reader;
    private final CommandListener commandListener;

    public IpcCommandReceiver(CommandDir dir, CommandListener commandListener) {
        this.commandListener = commandListener;
        this.reader = IpcChannel.reader(dir.getRequestPath(), RequestListenerEncoding::new);
    }

    public void run() {
        IpcReaders.decodeAll(reader, new MyRequestListener());
    }

    @NotThreadSafe
    private class MyRequestListener implements RequestListener {

        @Override
        public void runTests(SuiteConfiguration suiteConfiguration) {
            SuiteListener suiteListener = new NullSuiteListener();
            // TODO

            commandListener.runTests(suiteConfiguration, suiteListener);
        }

        @Override
        public void shutdown() {
            commandListener.shutdown();
        }
    }
}
