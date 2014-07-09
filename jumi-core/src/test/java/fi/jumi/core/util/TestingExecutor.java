// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.util;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.*;
import java.util.concurrent.*;

public class TestingExecutor extends ThreadPoolExecutor implements TestRule {

    private final List<Throwable> uncaughtExceptions = Collections.synchronizedList(new ArrayList<>());

    public TestingExecutor() {
        super(1, Integer.MAX_VALUE, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
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
                } finally {
                    shutdownNow();
                }
                awaitTermination(1000, TimeUnit.MILLISECONDS);
                for (Throwable uncaughtException : uncaughtExceptions) {
                    throw uncaughtException;
                }
            }
        };
    }
}
