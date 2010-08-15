// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.server;

import org.slf4j.*;

public class KillProcessOnUncaughtException implements Thread.UncaughtExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(KillProcessOnUncaughtException.class);

    public void uncaughtException(Thread t, Throwable e) {
        // the server is meant to be crash-only software, so it shall never exit cleanly
        logger.error("Uncaught exception in thread {}; the server will halt now", t.getName(), e);
        Runtime.getRuntime().halt(1);
    }
}
