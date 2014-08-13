// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.suite;

import fi.jumi.core.api.SuiteListener;
import fi.jumi.core.util.Boilerplate;

import javax.annotation.concurrent.ThreadSafe;
import java.io.PrintStream;
import java.util.concurrent.Executor;

@ThreadSafe
class InternalErrorReportingExecutor extends InternalErrorReporter implements Executor {

    private final Executor backingExecutor;

    public InternalErrorReportingExecutor(Executor backingExecutor, SuiteListener listener, PrintStream out) {
        super(listener, out);
        this.backingExecutor = backingExecutor;
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
                reportInternalError("Uncaught exception in thread " + Thread.currentThread().getName(), t);
            }
        }

        @Override
        public String toString() {
            return Boilerplate.toString(getClass(), command);
        }
    }
}
