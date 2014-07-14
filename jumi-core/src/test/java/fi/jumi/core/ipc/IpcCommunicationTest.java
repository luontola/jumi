// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import fi.jumi.core.config.*;
import fi.jumi.core.ipc.api.RequestListener;
import fi.jumi.core.ipc.dirs.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Path;

import static org.mockito.Mockito.*;

public class IpcCommunicationTest {

    @Rule
    public final TemporaryFolder tempDir = new TemporaryFolder();

    private Path baseDir;

    @Before
    public void setup() throws IOException {
        baseDir = tempDir.getRoot().toPath();
    }

    @Test
    public void launcher_sends_commands_to_daemon() throws IOException {
        RequestListener requestListener = mock(RequestListener.class);
        DaemonDir daemonDir = new DaemonDir(baseDir);
        CommandDir commandDir = daemonDir.createCommandDir();
        IpcCommandReader receiver = new IpcCommandReader(commandDir, requestListener);
        IpcCommandWriter sender = new IpcCommandWriter(commandDir);
        SuiteConfiguration suiteConfiguration = new SuiteConfigurationBuilder()
                .addJvmOptions("-some-options")
                .freeze();

        sender.tell().runTests(suiteConfiguration);
        sender.close();

        receiver.run();

        verify(requestListener).runTests(suiteConfiguration);
        verifyNoMoreInteractions(requestListener);
    }

    @Ignore // TODO
    @Test
    public void daemon_sends_suite_events_to_launcher() throws IOException {

    }
}
