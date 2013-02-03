// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.suite;

import fi.jumi.core.api.*;

import javax.annotation.concurrent.ThreadSafe;
import java.io.PrintStream;

@ThreadSafe
abstract class InternalErrorReporter {

    private final SuiteListener listener;
    private final PrintStream out;

    public InternalErrorReporter(SuiteListener listener, PrintStream out) {
        this.listener = listener;
        this.out = out;
    }

    protected void reportInternalError(String message, Throwable cause) {
        synchronized (out) {
            out.println(message);
            cause.printStackTrace(out);
        }
        listener.onInternalError(message, StackTrace.copyOf(cause));
    }
}
