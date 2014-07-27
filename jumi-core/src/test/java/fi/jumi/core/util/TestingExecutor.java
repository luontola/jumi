// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.util;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.Assert.assertTrue;

public class TestingExecutor extends ThreadPoolExecutor implements TestRule {

    private final List<Throwable> uncaughtExceptions = Collections.synchronizedList(new ArrayList<>());

    public TestingExecutor() {
        super(0, Integer.MAX_VALUE, 100, TimeUnit.MILLISECONDS, new SynchronousQueue<>());
    }

    @Override
    public void execute(Runnable command) {
        super.execute(() -> {
            try {
                command.run();
            } catch (Throwable t) {
                uncaughtExceptions.add(t);
            }
        });
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    base.evaluate();
                } catch (Throwable t) {
                    throw withUncaughtExceptions(t);
                } finally {
                    shutdownNow();
                }
                boolean terminated = awaitTermination(1000, TimeUnit.MILLISECONDS);
                if (!uncaughtExceptions.isEmpty()) {
                    throw withUncaughtExceptions(new AssertionError("There were uncaught exceptions in executor threads"));
                }
                assertTrue("Executor did not terminate properly", terminated);
            }
        };
    }

    private Throwable withUncaughtExceptions(Throwable t) {
        for (Throwable uncaughtException : uncaughtExceptions) {
            t.addSuppressed(uncaughtException);
        }
        return t;
    }
}
