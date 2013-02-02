// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.suite;

import fi.jumi.actors.listeners.FailureHandler;
import fi.jumi.core.api.*;

import javax.annotation.concurrent.ThreadSafe;
import java.io.PrintStream;

@ThreadSafe
class InternalErrorReportingFailureHandler implements FailureHandler {

    private final SuiteListener listener;
    private final PrintStream out;

    public InternalErrorReportingFailureHandler(SuiteListener listener, PrintStream out) {
        this.listener = listener;
        this.out = out;
    }

    @Override
    public void uncaughtException(Object actor, Object message, Throwable exception) {
        String description = "Uncaught exception in thread " + Thread.currentThread().getName() +
                " from " + actor + " when processing " + message;
        synchronized (out) {
            out.println(description);
            exception.printStackTrace(out);
        }
        listener.onInternalError(description, StackTrace.copyOf(exception));
    }
}
