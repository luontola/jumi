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
import fi.jumi.core.events.suiteListener.OnSuiteFinishedEvent;
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

    private int iterations = 0;

    @Override
    public void runTests(final SuiteConfiguration suite) {
        final long start = System.currentTimeMillis();
        SuiteListener suiteListener = new SuiteListenerEventizer().newFrontend(new MessageSender<Event<SuiteListener>>() {
            @Override
            public void send(Event<SuiteListener> message) {

                // don't end the suite until we have done enough iterations
                if (message.getClass() != OnSuiteFinishedEvent.class) {
                    sender.send(message);
                    return;
                }

                long end = System.currentTimeMillis();
                long duration = end - start;
                System.out.println(iterations + ": took " + duration + " ms");

                iterations++;
                if (iterations < 11) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            // release resources
                            suiteFactory.close();
                            try {
                                // must be done in a new thread, because else the above call would interrupt this
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }

                            // restart suite
                            runTests(suite);
                        }
                    }).start();
                } else {
                    sender.send(message); // last iteration, end the suite
                }
            }
        });
        suiteFactory.configure(suite);
        suiteFactory.start(suiteListener);
    }

    @Override
    public void shutdown() {
        shutdownHook.run();
    }
}
