// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.server;

import com.google.inject.*;
import net.orfjackal.dimdwarf.events.SystemLifecycleListener;
import org.slf4j.*;

import javax.annotation.concurrent.NotThreadSafe;

@Singleton
@NotThreadSafe
public class ServerLifecycleManager {
    private static final Logger logger = LoggerFactory.getLogger(ServerLifecycleManager.class);

    private final SystemLifecycleListener[] listeners;
    private State state = State.NOT_STARTED;

    @Inject
    public ServerLifecycleManager(SystemLifecycleListener[] listeners) {
        this.listeners = listeners;
    }

    public State getState() {
        return state;
    }

    public void start() {
        assert state.equals(State.NOT_STARTED) : "was " + state;
        state = State.STARTED;

        logger.info("Startup sequence initiated...");
        for (SystemLifecycleListener listener : listeners) {
            try {
                listener.onStartup();
            } catch (Throwable t) {
                logger.error("Exception during startup sequence", t);
                throw new RuntimeException(t);
            }
        }
        logger.info("Started up.");
    }

    public void shutdown() {
        assert state.equals(State.STARTED) : "was " + state;
        state = State.SHUT_DOWN;

        logger.info("Shutdown sequence initiated...");
        for (SystemLifecycleListener listener : listeners) { // TODO: send shutdown events in reverse order?
            try {
                listener.onShutdown();
            } catch (Throwable t) {
                logger.error("Exception during shutdown sequence", t);
            }
        }
        logger.info("Shutting down.");
    }

    public enum State {
        NOT_STARTED, STARTED, SHUT_DOWN
    }
}
