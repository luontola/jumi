// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.junit;

import fi.jumi.api.drivers.*;
import org.junit.runner.*;
import org.junit.runner.notification.*;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;
import java.util.regex.Pattern;

@NotThreadSafe
public class JUnitRunListenerAdapter extends RunListener {

    private static final Pattern JAVA_CLASS_NAME_PATTERN = Pattern.compile("[\\._$\\p{Alnum}]+");

    private final SuiteNotifier notifier;
    private final Deque<TestNotifier> activeTestsStack = new ArrayDeque<>();
    private final Map<Description, TestId> descriptionIds = new HashMap<>();
    private Description rootDescription;

    public JUnitRunListenerAdapter(SuiteNotifier notifier) {
        this.notifier = notifier;
    }

    @Override
    public void testRunStarted(Description description) {
        rootDescription = description;
        fireTestFound(TestId.ROOT, description);
    }

    private void fireTestFound(TestId testId, Description description) {
        TestId previousValue = descriptionIds.put(description, testId);
        if (previousValue == null) {
            notifier.fireTestFound(testId, formatTestName(description));
        }

        TestId childId = testId.getFirstChild();
        for (Description child : description.getChildren()) {
            fireTestFound(childId, child);
            childId = childId.getNextSibling();
        }
    }

    private static String formatTestName(Description description) {
        String methodName = description.getMethodName();
        if (methodName != null) {
            return methodName;
        }
        String className = description.getClassName();
        if (isJavaClassName(className)) {
            return simpleClassName(className);
        }
        // not a class name, but actually free-form text
        return className;
    }

    private static boolean isJavaClassName(String s) {
        return JAVA_CLASS_NAME_PATTERN.matcher(s).matches();
    }

    private static String simpleClassName(String name) {
        name = name.substring(name.lastIndexOf('.') + 1);
        name = name.substring(name.lastIndexOf('$') + 1);
        return name;
    }

    @Override
    public void testRunFinished(Result result) {
    }

    @Override
    public void testStarted(Description description) {
        TestId id = descriptionIds.get(description);
        if (id == null) {
            // Hoping that the runner added this description as a child to the top-level description
            fireTestFound(TestId.ROOT, rootDescription);
            id = descriptionIds.get(description);
        }
        if (id == null) {
            // Fallback if we have no way of knowing this description's parent
            id = nextUnassignedChildOf(TestId.ROOT);
            fireTestFound(id, description);
        }
        startTestAndItsParents(id);
    }

    private TestId nextUnassignedChildOf(TestId parent) {
        Set<TestId> assignedIds = new HashSet<>(descriptionIds.values());
        TestId id = parent.getFirstChild();
        while (assignedIds.contains(id)) {
            id = id.getNextSibling();
        }
        return id;
    }

    private void startTestAndItsParents(TestId testId) {
        if (!testId.isRoot()) {
            startTestAndItsParents(testId.getParent());
        }
        TestNotifier tn = notifier.fireTestStarted(testId);
        activeTestsStack.push(tn);
    }

    @Override
    public void testFinished(Description description) {
        finishAllTests();
    }

    private void finishAllTests() {
        while (!activeTestsStack.isEmpty()) {
            TestNotifier tn = activeTestsStack.pop();
            tn.fireTestFinished();
        }
    }

    @Override
    public void testFailure(Failure failure) {
        TestNotifier tn = activeTestsStack.peek();
        tn.fireFailure(failure.getException());
    }

    @Override
    public void testAssumptionFailure(Failure failure) {
        // TODO: implement ignoring tests into Jumi, then fire the appropriate event here
        failure.getException().printStackTrace();
    }

    @Override
    public void testIgnored(Description description) {
        // TODO: implement ignoring tests into Jumi, then fire the appropriate event here
    }
}
