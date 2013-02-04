// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.daemon;

import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.queue.MessageSender;
import fi.jumi.core.CommandListener;
import fi.jumi.core.api.SuiteListener;
import fi.jumi.core.config.SuiteConfiguration;
import fi.jumi.core.events.SuiteListenerEventizer;
import fi.jumi.core.network.*;
import fi.jumi.core.suite.SuiteFactory;
import fi.jumi.core.util.timeout.*;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class DaemonNetworkEndpoint implements NetworkEndpoint<Event<CommandListener>, Event<SuiteListener>>, CommandListener {

    private final SuiteFactory suiteFactory;
    private final Runnable shutdownHook;
    private final Timeout startupTimeout;
    private final VacancyTimeout connections;

    private MessageSender<Event<SuiteListener>> sender;

    public DaemonNetworkEndpoint(SuiteFactory suiteFactory, Runnable shutdownHook, Timeout startupTimeout, Timeout idleTimeout) {
        this.suiteFactory = suiteFactory;
        this.shutdownHook = shutdownHook;
        this.startupTimeout = startupTimeout;
        this.connections = new VacancyTimeout(idleTimeout);
    }

    @Override
    public void onConnected(NetworkConnection connection, MessageSender<Event<SuiteListener>> sender) {
        this.sender = sender;
        startupTimeout.cancel();
        connections.checkIn();
    }

    @Override
    public void onDisconnected() {
        connections.checkOut();
    }

    @Override
    public void onMessage(Event<CommandListener> message) {
        message.fireOn(this);
    }

    @Override
    public void runTests(SuiteConfiguration suite) {
        SuiteListener suiteListener = new SuiteListenerEventizer().newFrontend(sender);
        suiteFactory.configure(suite);
        suiteFactory.start(suiteListener);
    }

    @Override
    public void shutdown() {
        shutdownHook.run();
    }
}
