// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.junit;

import fi.jumi.api.drivers.*;
import org.junit.runner.*;
import org.junit.runner.notification.*;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;

@NotThreadSafe
public class JUnitRunListenerAdapter extends RunListener {

    private final SuiteNotifier notifier;
    private TestNotifier tn3;
    private TestNotifier tn2;
    private TestNotifier tn1;

    private final Map<Description, TestId> descriptionIds = new HashMap<>();

    public JUnitRunListenerAdapter(SuiteNotifier notifier) {
        this.notifier = notifier;
    }

    @Override
    public void testRunStarted(Description description) throws Exception {
        System.out.println("testRunStarted " + description + "; children " + description.getChildren());

        descriptionIds.put(description, TestId.ROOT);
        notifier.fireTestFound(TestId.ROOT, format(description));

        TestId id1 = TestId.ROOT.getFirstChild();
        for (Description level1 : description.getChildren()) {
            descriptionIds.put(level1, id1);
            notifier.fireTestFound(id1, format(level1));

            // TODO: recursion
            for (Description level2 : level1.getChildren()) {
                TestId id2 = id1.getFirstChild();
                descriptionIds.put(level2, id2);
                notifier.fireTestFound(id2, format(level2));
            }

            id1 = id1.getNextSibling();
        }
    }

    private static String format(Description description) {
        String methodName = description.getMethodName();
        if (methodName != null) {
            return methodName;
        } else {
            // TODO: what if the description is free-form text? should we support such custom JUnit runners?
            return simpleClassName(description.getClassName());
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
        // TODO: handle it if id is null - new tests were discovered after testRunStarted
        TestId id = descriptionIds.get(description);

        // TODO: recursion
        if (!id.getParent().isRoot()) {
            tn3 = notifier.fireTestStarted(id.getParent().getParent());
        }
        if (!id.isRoot()) {
            tn2 = notifier.fireTestStarted(id.getParent());
        }
        tn1 = notifier.fireTestStarted(id);
    }

    @Override
    public void testFinished(Description description) throws Exception {
        System.out.println("testFinished " + description);
        // TODO: recursion
        tn1.fireTestFinished();
        if (tn2 != null) {
            tn2.fireTestFinished();
        }
        if (tn3 != null) {
            tn3.fireTestFinished();
        }
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
