// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.suite;

import fi.jumi.core.api.*;

import javax.annotation.concurrent.ThreadSafe;
import java.io.PrintStream;
import java.util.concurrent.Executor;

@ThreadSafe
class InternalErrorReportingExecutor implements Executor {

    private final Executor backingExecutor;
    private final SuiteListener listener;
    private final PrintStream out;

    public InternalErrorReportingExecutor(Executor backingExecutor, SuiteListener listener, PrintStream out) {
        this.backingExecutor = backingExecutor;
        this.listener = listener;
        this.out = out;
    }

    @Override
    public void execute(Runnable command) {
        backingExecutor.execute(new InternalErrorReporter(command));
    }


    @ThreadSafe
    private class InternalErrorReporter implements Runnable {
        private final Runnable command;

        public InternalErrorReporter(Runnable command) {
            this.command = command;
        }

        @Override
        public void run() {
            try {
                command.run();
            } catch (Throwable t) {
                String description = "Uncaught exception in thread " + Thread.currentThread().getName();
                synchronized (out) {
                    out.println(description);
                    t.printStackTrace(out);
                }
                listener.onInternalError(description, StackTrace.copyOf(t));
            }
        }
    }
}
