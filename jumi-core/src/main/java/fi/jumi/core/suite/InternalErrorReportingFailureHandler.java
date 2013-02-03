// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.suite;

import fi.jumi.actors.listeners.FailureHandler;
import fi.jumi.core.api.SuiteListener;

import javax.annotation.concurrent.ThreadSafe;
import java.io.PrintStream;

@ThreadSafe
class InternalErrorReportingFailureHandler extends InternalErrorReporter implements FailureHandler {

    public InternalErrorReportingFailureHandler(SuiteListener listener, PrintStream out) {
        super(listener, out);
    }

    @Override
    public void uncaughtException(Object actor, Object message, Throwable exception) {
        reportInternalError("Uncaught exception in thread " + Thread.currentThread().getName() +
                " from " + actor + " when processing " + message, exception);
    }
}
