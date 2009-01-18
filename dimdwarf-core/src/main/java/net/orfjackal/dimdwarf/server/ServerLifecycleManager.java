/*
 * This file is part of Dimdwarf Application Server <http://dimdwarf.sourceforge.net/>
 *
 * Copyright (c) 2008-2009, Esko Luontola. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.orfjackal.dimdwarf.server;

import com.google.inject.*;
import net.orfjackal.dimdwarf.events.SystemLifecycleListener;
import org.slf4j.*;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * @author Esko Luontola
 * @since 28.11.2008
 */
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
