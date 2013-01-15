// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.junit;

import fi.jumi.api.drivers.*;
import org.junit.runner.*;
import org.junit.runner.notification.*;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class JUnitRunListenerAdapter extends RunListener {

    private final SuiteNotifier notifier;
    private TestNotifier classTn;
    private TestNotifier methodTn;

    public JUnitRunListenerAdapter(SuiteNotifier notifier) {
        this.notifier = notifier;
    }

    @Override
    public void testRunStarted(Description description) throws Exception {
        System.out.println("testRunStarted " + description + "; children " + description.getChildren());

        // TODO: what if the description's "class name" is free-form text? should we support such custom JUnit runners?
        notifier.fireTestFound(TestId.ROOT, simpleClassName(description.getClassName()));

        // TODO: calculate test ids
        for (Description level1 : description.getChildren()) {
            notifier.fireTestFound(TestId.of(0), level1.getMethodName());

//            for (Description level2 : level1.getChildren()) {
//                notifier.fireTestFound(TestId.of(0, 0), level2.getMethodName());
//            }
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
        // TODO
    }

    @Override
    public void testStarted(Description description) throws Exception {
        System.out.println("testStarted " + description);
        // TODO
        classTn = notifier.fireTestStarted(TestId.ROOT);
        methodTn = notifier.fireTestStarted(TestId.of(0));
    }

    @Override
    public void testFinished(Description description) throws Exception {
        System.out.println("testFinished " + description);
        // TODO
        methodTn.fireTestFinished();
        classTn.fireTestFinished();
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        System.out.println("testFailure " + failure);
        // TODO
    }

    @Override
    public void testAssumptionFailure(Failure failure) {
        System.out.println("testAssumptionFailure " + failure);
        // TODO
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        System.out.println("testIgnored " + description);
        // TODO
    }
}
