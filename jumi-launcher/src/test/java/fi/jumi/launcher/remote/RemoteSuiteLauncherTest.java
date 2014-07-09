// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.remote;

import fi.jumi.actors.ActorRef;
import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.queue.*;
import fi.jumi.core.api.*;
import fi.jumi.core.config.*;
import fi.jumi.core.events.CommandListenerEventizer;
import fi.jumi.core.events.suiteListener.*;
import fi.jumi.core.ipc.api.CommandListener;
import fi.jumi.core.util.SpyListener;
import fi.jumi.launcher.FakeActorThread;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class RemoteSuiteLauncherTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private final CommandListener daemon = mock(CommandListener.class);
    private final MessageSender<Event<CommandListener>> senderToDaemon = new CommandListenerEventizer().newBackend(daemon);
    private final SpyDaemonSummoner daemonSummoner = new SpyDaemonSummoner();
    private final SuiteConfiguration dummySuiteConfig = new SuiteConfiguration();
    private final DaemonConfiguration dummyDaemonConfig = new DaemonConfiguration();

    private final RemoteSuiteLauncher suiteLauncher =
            new RemoteSuiteLauncher(new FakeActorThread(), ActorRef.<DaemonSummoner>wrap(daemonSummoner));

    private final MessageQueue<Event<SuiteListener>> suiteListener = new MessageQueue<>();

    @Test
    public void sends_RunTests_command_to_the_daemon_when_it_connects() {
        SuiteConfiguration config = new SuiteConfigurationBuilder()
                .addToClasspath(Paths.get("dependency.jar"))
                .setTestClasses("FooTest")
                .freeze();

        suiteLauncher.runTests(config, dummyDaemonConfig, suiteListener);
        callback().tell().onConnected(null, senderToDaemon);

        verify(daemon).runTests(config);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void forwards_messages_from_daemon_to_the_SuiteListener() {
        Event<SuiteListener> expectedEvent = mock(Event.class);
        suiteLauncher.runTests(dummySuiteConfig, dummyDaemonConfig, suiteListener);
        callback().tell().onConnected(null, senderToDaemon);

        callback().tell().onMessage(expectedEvent);

        assertThat(suiteListener.poll(), is(expectedEvent));
    }

    @Test
    public void can_send_shutdown_command_to_the_daemon() {
        suiteLauncher.runTests(dummySuiteConfig, dummyDaemonConfig, suiteListener);
        callback().tell().onConnected(null, senderToDaemon);

        suiteLauncher.shutdownDaemon();

        verify(daemon).shutdown();
    }

    @Test
    public void shutdown_command_fails_if_the_daemon_is_not_connected() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("daemon not connected");

        suiteLauncher.shutdownDaemon();
    }


    // reporting a daemon which dies unexpectedly

    @Test
    public void reports_an_internal_error_if_the_daemon_disconnects_between_onSuiteStarted_and_onSuiteFinished() {
        SpyListener<SuiteListener> spy = new SpyListener<>(SuiteListener.class);
        SuiteListener expect = spy.getListener();

        expect.onSuiteStarted();
        expect.onInternalError("The test runner daemon process disconnected or died unexpectedly", StackTrace.from(new Exception("disconnected")));
        expect.onSuiteFinished();

        spy.replay();

        suiteLauncher.runTests(dummySuiteConfig, dummyDaemonConfig, new EventToSuiteListener(expect));
        callback().tell().onConnected(null, senderToDaemon);
        callback().tell().onMessage(new OnSuiteStartedEvent()); // suite is in progress
        callback().tell().onDisconnected(); // daemon dies, network connection is disconnection

        spy.verify();
    }

    @Test
    public void does_not_report_any_errors_if_daemon_disconnects_before_onSuiteStarted() {
        SpyListener<SuiteListener> spy = new SpyListener<>(SuiteListener.class);
        SuiteListener expect = spy.getListener();

        // expect no events; another component will report whether starting the daemon failed

        spy.replay();

        suiteLauncher.runTests(dummySuiteConfig, dummyDaemonConfig, new EventToSuiteListener(expect));
        callback().tell().onConnected(null, senderToDaemon);
        callback().tell().onDisconnected();

        spy.verify();
    }

    @Test
    public void does_not_report_any_errors_if_daemon_disconnects_after_onSuiteFinished() {
        SpyListener<SuiteListener> spy = new SpyListener<>(SuiteListener.class);
        SuiteListener expect = spy.getListener();

        expect.onSuiteStarted();
        expect.onSuiteFinished();

        spy.replay();

        suiteLauncher.runTests(dummySuiteConfig, dummyDaemonConfig, new EventToSuiteListener(expect));
        callback().tell().onConnected(null, senderToDaemon);
        callback().tell().onMessage(new OnSuiteStartedEvent());
        callback().tell().onMessage(new OnSuiteFinishedEvent());
        callback().tell().onDisconnected();

        spy.verify();
    }


    // helpers

    private ActorRef<DaemonListener> callback() {
        return daemonSummoner.lastListener;
    }

    private static class SpyDaemonSummoner implements DaemonSummoner {

        public ActorRef<DaemonListener> lastListener;

        @Override
        public void connectToDaemon(SuiteConfiguration suiteConfiguration,
                                    DaemonConfiguration daemonConfiguration,
                                    ActorRef<DaemonListener> listener) {
            lastListener = listener;
        }
    }
}
