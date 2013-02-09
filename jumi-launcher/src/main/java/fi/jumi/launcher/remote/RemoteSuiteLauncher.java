// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.remote;

import fi.jumi.actors.*;
import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.queue.MessageSender;
import fi.jumi.core.CommandListener;
import fi.jumi.core.api.*;
import fi.jumi.core.config.*;
import fi.jumi.core.events.CommandListenerEventizer;
import fi.jumi.core.events.suiteListener.*;
import fi.jumi.core.network.NetworkConnection;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class RemoteSuiteLauncher implements SuiteLauncher, DaemonListener {

    private final ActorThread currentThread;
    private final ActorRef<DaemonSummoner> daemonSummoner;

    private SuiteConfiguration suiteConfiguration;
    private MessageSender<Event<SuiteListener>> suiteListener;
    private CommandListener daemon;
    private final SuiteState suiteState = new SuiteState();

    public RemoteSuiteLauncher(ActorThread currentThread, ActorRef<DaemonSummoner> daemonSummoner) {
        this.currentThread = currentThread;
        this.daemonSummoner = daemonSummoner;
    }

    @Override
    public void runTests(SuiteConfiguration suiteConfiguration,
                         DaemonConfiguration daemonConfiguration,
                         MessageSender<Event<SuiteListener>> suiteListener) {
        this.suiteConfiguration = suiteConfiguration;
        this.suiteListener = suiteListener;
        daemonSummoner.tell().connectToDaemon(suiteConfiguration, daemonConfiguration, self());
    }

    @Override
    public void shutdownDaemon() {
        if (daemon == null) {
            throw new IllegalStateException("cannot shutdown; daemon not connected");
        }
        daemon.shutdown();
    }

    @Override
    public void onConnected(NetworkConnection connection, MessageSender<Event<CommandListener>> daemon) {
        this.daemon = new CommandListenerEventizer().newFrontend(daemon);
        this.daemon.runTests(suiteConfiguration);
    }

    @Override
    public void onMessage(Event<SuiteListener> message) {
        suiteListener.send(message);
        message.fireOn(suiteState);
    }

    @Override
    public void onDisconnected() {
        if (suiteState.inProgress) {
            suiteListener.send(new OnInternalErrorEvent("The test runner daemon process disconnected or died unexpectedly",
                    StackTrace.copyOf(new Exception("disconnected"))));
            suiteListener.send(new OnSuiteFinishedEvent());
        }

        // TODO: Should we reconnect or something? For now let's defer implementing this, because
        // this feature might not be needed if we switch to communicating over memory-mapped files.
    }


    // actor helpers

    private ActorRef<DaemonListener> self() {
        return currentThread.bindActor(DaemonListener.class, this);
    }


    @NotThreadSafe
    private static class SuiteState extends NullSuiteListener {

        public boolean inProgress = false;

        @Override
        public void onSuiteStarted() {
            inProgress = true;
        }

        @Override
        public void onSuiteFinished() {
            inProgress = false;
        }
    }
}
