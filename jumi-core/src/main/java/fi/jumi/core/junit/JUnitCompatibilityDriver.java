// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.junit;

import fi.jumi.api.drivers.*;
import org.junit.runner.*;
import org.junit.runner.notification.*;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.concurrent.Executor;

@NotThreadSafe
public class JUnitCompatibilityDriver extends Driver {

    public JUnitCompatibilityDriver(ClassLoader classLoader) {

    }

    @Override
    public void findTests(Class<?> testClass, SuiteNotifier notifier, Executor executor) {
        executor.execute(new JUnitRunner(testClass, new RunListenerAdapter(notifier)));
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
            junit.run(testClass);
        }
    }

    @NotThreadSafe
    private static class RunListenerAdapter extends RunListener {
        private final SuiteNotifier notifier;
        private TestNotifier tn;

        public RunListenerAdapter(SuiteNotifier notifier) {
            this.notifier = notifier;
        }

        @Override
        public void testRunStarted(Description description) throws Exception {
            System.out.println("testRunStarted " + description + "; children " + description.getChildren());
            for (Description child : description.getChildren()) {
                System.out.println("child = " + child);
                System.out.println("child.isSuite() = " + child.isSuite());
                System.out.println("child.getDisplayName() = " + child.getDisplayName());
                System.out.println("child.getClassName() = " + child.getClassName());
                System.out.println("child.getMethodName() = " + child.getMethodName());

                // TODO: what if the description's "class name" is free-form text? should we support such custom JUnit runners?
                notifier.fireTestFound(TestId.ROOT, simpleClassName(child.getClassName()));

                for (Description grandchild : child.getChildren()) {
                    // TODO: calculate test ids
                    notifier.fireTestFound(TestId.of(0), grandchild.getMethodName());
                }
            }
        }

        private static String simpleClassName(String name) {
            name = name.substring(name.lastIndexOf('.') + 1);
            name = name.substring(name.lastIndexOf('$') + 1);
            return name;
        }

        @Override
        public void testRunFinished(Result result) throws Exception {
            System.out.println("testRunFinished " + result);
        }

        @Override
        public void testStarted(Description description) throws Exception {
            System.out.println("testStarted " + description);
            tn = notifier.fireTestStarted(TestId.ROOT);
        }

        @Override
        public void testFinished(Description description) throws Exception {
            System.out.println("testFinished " + description);
            tn.fireTestFinished();
        }

        @Override
        public void testFailure(Failure failure) throws Exception {
            System.out.println("testFailure " + failure);
        }

        @Override
        public void testAssumptionFailure(Failure failure) {
            System.out.println("testAssumptionFailure " + failure);
        }

        @Override
        public void testIgnored(Description description) throws Exception {
            System.out.println("testIgnored " + description);
        }
    }
}
