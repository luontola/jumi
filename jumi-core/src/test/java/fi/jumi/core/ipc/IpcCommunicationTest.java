// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import fi.jumi.core.api.SuiteListener;
import fi.jumi.core.config.*;
import fi.jumi.core.ipc.api.CommandListener;
import fi.jumi.core.ipc.dirs.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;

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
    public void launcher_sends_commands_to_daemon() throws IOException {
        CommandListener daemonSide = mock(CommandListener.class);

        IpcCommandReceiver receiver = new IpcCommandReceiver(daemonDir, commandDir, daemonSide);
        IpcCommandSender sender = new IpcCommandSender(commandDir);
        SuiteConfiguration suiteConfiguration = new SuiteConfigurationBuilder()
                .addJvmOptions("-some-options")
                .freeze();

        sender.runTests(suiteConfiguration, null);
        sender.shutdown();
        sender.close();

        receiver.run();

        verify(daemonSide).runTests(eq(suiteConfiguration), any(SuiteListener.class));
        verify(daemonSide).shutdown();
        verifyNoMoreInteractions(daemonSide);
    }

    @Test
    public void daemon_sends_suite_events_to_launcher() throws IOException {
        SuiteListener launcherSide = mock(SuiteListener.class);
        SpyCommandListener commandProcessor = new SpyCommandListener();

        IpcCommandReceiver receiver = new IpcCommandReceiver(daemonDir, commandDir, commandProcessor);
        IpcCommandSender sender = new IpcCommandSender(commandDir);

        sender.runTests(new SuiteConfiguration(), launcherSide);
        sender.close();

        receiver.run();

        // TODO: this should be asynchronous
        SuiteListener daemonSide = commandProcessor.suiteListener;
        daemonSide.onSuiteStarted();
        daemonSide.onSuiteFinished();

        sender.poll_UGLY_HACK();

        // TODO: this should be asynchronous
        verify(launcherSide).onSuiteStarted();
        verify(launcherSide).onSuiteFinished();
        verifyNoMoreInteractions(launcherSide);
    }

    private static class SpyCommandListener implements CommandListener {
        SuiteConfiguration suiteConfiguration;
        SuiteListener suiteListener;

        @Override
        public void runTests(SuiteConfiguration suiteConfiguration, SuiteListener suiteListener) {
            this.suiteConfiguration = suiteConfiguration;
            this.suiteListener = suiteListener;
        }

        @Override
        public void shutdown() {
        }
    }
}
