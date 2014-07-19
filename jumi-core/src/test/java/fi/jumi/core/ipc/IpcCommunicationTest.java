// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import fi.jumi.core.api.SuiteListener;
import fi.jumi.core.config.*;
import fi.jumi.core.ipc.api.CommandListener;
import fi.jumi.core.ipc.channel.*;
import fi.jumi.core.ipc.dirs.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.concurrent.Future;

import static org.mockito.Mockito.*;

public class IpcCommunicationTest {

    @Rule
    public final TemporaryFolder tempDir = new TemporaryFolder();

    private DaemonDir daemonDir;
    private CommandDir commandDir;

    @Before
    public void setup() throws IOException {
        daemonDir = new DaemonDir(tempDir.getRoot().toPath());
        commandDir = daemonDir.createCommandDir();
    }

    @Test
    public void launcher_sends_commands_to_daemon() throws Exception {
        CommandListener daemonSide = mock(CommandListener.class);

        IpcCommandReceiver receiver = new IpcCommandReceiver(daemonDir, commandDir, daemonSide);
        IpcCommandSender sender = new IpcCommandSender(commandDir);
        SuiteConfiguration suiteConfiguration = new SuiteConfigurationBuilder()
                .addJvmOptions("-some-options")
                .freeze();

        sender.runTests(suiteConfiguration);
        sender.shutdown();
        sender.close();

        receiver.run();

        verify(daemonSide).runTests(eq(suiteConfiguration), any(SuiteListener.class));
        verify(daemonSide).shutdown();
        verifyNoMoreInteractions(daemonSide);
    }

    @Test
    public void daemon_sends_suite_events_to_launcher() throws Exception {
        IpcCommandSender sender = new IpcCommandSender(commandDir);
        Future<IpcReader<SuiteListener>> suiteReader = sender.runTests(new SuiteConfiguration());
        sender.close();

        // TODO: use the DirectoryObserver (which then creates IpcCommandReceiver)
        IpcCommandReceiver receiver = new IpcCommandReceiver(daemonDir, commandDir, new CommandListener() {
            @Override
            public void runTests(SuiteConfiguration suiteConfiguration, SuiteListener suiteListener) {
                // this happens on daemon side
                suiteListener.onSuiteStarted();
                suiteListener.onSuiteFinished();
            }

            @Override
            public void shutdown() {
            }
        });
        receiver.run(); // XXX: should be in a worker thread

        sender.readResponses(); // XXX: should be in a worker thread

        SuiteListener suiteListener = mock(SuiteListener.class);
        IpcReaders.decodeAll(suiteReader.get(), suiteListener);
        verify(suiteListener).onSuiteStarted();
        verify(suiteListener).onSuiteFinished();
        verifyNoMoreInteractions(suiteListener);
    }
}
