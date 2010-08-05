// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.server;

import com.google.inject.*;
import net.orfjackal.dimdwarf.scheduler.TaskThreadPool;

import java.util.*;
import java.util.logging.*;

import static net.orfjackal.dimdwarf.server.ServerLifecycleManager.State.STARTED;

public class TestServer {

    // FIXME: ServerLifecycleManager will be removed/refactored in new architecture

    private final Injector injector;
    private final ServerLifecycleManager server;
    private final List<Logger> logsToReset = new ArrayList<Logger>();

    public TestServer(Module... modules) {
        injector = Guice.createInjector(modules);
        server = injector.getInstance(ServerLifecycleManager.class);
    }

    public Injector getInjector() {
        return injector;
    }

    public void hideStartupShutdownLogs() {
        changeLoggingLevel(ServerLifecycleManager.class, Level.WARNING);
        changeLoggingLevel(TaskThreadPool.class, Level.WARNING);
    }

    public void changeLoggingLevel(Class<?> clazz, Level level) {
        // TODO: replace JDK logging with Lockback (or even better, decouple the code from logging)
        Logger logger = Logger.getLogger(clazz.getName());
        logger.setLevel(level);
        logsToReset.add(logger);
    }

    public void start() {
        server.start();
    }

    public void shutdownIfRunning() {
        if (server.getState().equals(STARTED)) {
            server.shutdown();
        }
        for (Logger logger : logsToReset) {
            logger.setLevel(Level.ALL);
        }
    }
}
