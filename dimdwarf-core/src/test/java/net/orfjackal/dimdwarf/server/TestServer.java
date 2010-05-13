// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.server;

import com.google.inject.*;
import net.orfjackal.dimdwarf.scheduler.TaskThreadPool;

import java.util.*;
import java.util.logging.*;

import static net.orfjackal.dimdwarf.server.ServerLifecycleManager.State.STARTED;

public class TestServer {

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
