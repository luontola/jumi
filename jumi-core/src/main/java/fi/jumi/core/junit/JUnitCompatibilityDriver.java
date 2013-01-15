// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.junit;

import fi.jumi.api.drivers.*;
import org.junit.runner.*;
import org.junit.runner.notification.RunListener;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.concurrent.Executor;

@NotThreadSafe
public class JUnitCompatibilityDriver extends Driver {

    @Override
    public void findTests(Class<?> testClass, SuiteNotifier notifier, Executor executor) {
        executor.execute(new JUnitRunner(testClass, new JUnitRunListenerAdapter(notifier)));
    }


    @NotThreadSafe
    private static class JUnitRunner implements Runnable {
        private final Class<?> testClass;
        private final RunListener listener;

        public JUnitRunner(Class<?> testClass, RunListener listener) {
            this.testClass = testClass;
            this.listener = listener;
        }

        @Override
        public void run() {
            JUnitCore junit = new JUnitCore();
            junit.addListener(listener);
            junit.run(Request.aClass(testClass));
        }
    }
}
