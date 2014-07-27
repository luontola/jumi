// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import fi.jumi.actors.*;
import fi.jumi.actors.eventizers.dynamic.DynamicEventizerProvider;
import fi.jumi.actors.listeners.*;
import fi.jumi.core.Timeouts;
import fi.jumi.core.api.SuiteListener;
import fi.jumi.core.config.*;
import fi.jumi.core.ipc.api.CommandListener;
import fi.jumi.core.ipc.channel.*;
import fi.jumi.core.ipc.dirs.*;
import fi.jumi.core.util.TestingExecutor;
import org.junit.*;
import org.junit.rules.*;

import java.io.IOException;
import java.util.concurrent.Future;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class IpcCommunicationTest {

    @Rule
    public final Timeout timeout = Timeouts.forUnitTest();

    @Rule
    public final TemporaryFolder tempDir = new TemporaryFolder();

    @Rule
    public final TestingExecutor executor = new TestingExecutor();

    private DaemonDir daemonDir;
    private CommandDir commandDir;
    private ActorThread actorThread;

    @Before
    public void setup() throws IOException {
        daemonDir = new DaemonDir(tempDir.getRoot().toPath());
        commandDir = daemonDir.createCommandDir();
        Actors actors = new MultiThreadedActors(
                executor,
                new DynamicEventizerProvider(),
                new CrashEarlyFailureHandler(),
                new NullMessageListener()
        );
        actorThread = actors.startActorThread();
    }

    @Test
    public void launcher_tells_daemon_to_runTests_and_daemon_replies() throws Exception {
        SuiteConfiguration expectedSuiteConfiguration = new SuiteConfigurationBuilder()
                .addJvmOptions("-some-options")
                .freeze();

        executor.execute(new CommandsDirectoryObserver(daemonDir, executor, actorThread, new CommandListener() {
            @Override
            public void runTests(SuiteConfiguration suiteConfiguration, ActorRef<SuiteListener> suiteListener) {
                // this happens on daemon side
                assertThat(suiteConfiguration, is(expectedSuiteConfiguration));
                suiteListener.tell().onSuiteStarted();
                suiteListener.tell().onSuiteFinished();
            }

            @Override
            public void shutdown() {
            }
        }));

        IpcCommandSender sender = new IpcCommandSender(commandDir, executor);
        Future<IpcReader<SuiteListener>> suiteReader = sender.runTests(expectedSuiteConfiguration);
        sender.close();

        SuiteListener suiteListener = mock(SuiteListener.class);
        IpcReaders.decodeAll(suiteReader.get(), suiteListener);
        verify(suiteListener).onSuiteStarted();
        verify(suiteListener).onSuiteFinished();
        verifyNoMoreInteractions(suiteListener);
    }
}
