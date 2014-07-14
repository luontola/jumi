// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import fi.jumi.core.config.*;
import fi.jumi.core.ipc.api.CommandListener;
import fi.jumi.core.ipc.dirs.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;

import static org.mockito.Mockito.*;

public class IpcCommandReadingAndWritingTest {

    @Rule
    public final TemporaryFolder tempDir = new TemporaryFolder();

    @Test
    public void receives_commands() throws IOException {
        CommandListener listener = mock(CommandListener.class);
        DaemonDir daemonDir = new DaemonDir(tempDir.newFolder("daemonDir").toPath());
        CommandDir commandDir = daemonDir.createCommandDir();
        IpcCommandReader receiver = new IpcCommandReader(commandDir, listener);
        IpcCommandWriter sender = new IpcCommandWriter(commandDir);
        SuiteConfiguration suiteConfiguration = new SuiteConfigurationBuilder()
                .addJvmOptions("-some-options")
                .freeze();

        sender.tell().runTests(suiteConfiguration);
        sender.tell().shutdown();
        sender.close();

        receiver.run();

        verify(listener).runTests(suiteConfiguration);
        verify(listener).shutdown();
        verifyNoMoreInteractions(listener);
    }
}
