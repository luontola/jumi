// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.results;

import fi.jumi.api.drivers.TestId;
import fi.jumi.core.api.*;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;

@NotThreadSafe
public class SuiteResultsSummary extends NullRunVisitor {

    private final Set<GlobalTestId> failedTests = new HashSet<>();
    private final Set<GlobalTestId> allTests = new HashSet<>();

    public int getPassingTests() {
        return getTotalTests() - getFailingTests();
    }

    public int getFailingTests() {
        return failedTests.size();
    }

    public int getTotalTests() {
        return allTests.size();
    }

    @Override
    public void onTestStarted(RunId runId, TestFile testFile, TestId testId) {
        allTests.add(new GlobalTestId(testFile, testId));
    }

    @Override
    public void onFailure(RunId runId, TestFile testFile, TestId testId, StackTrace cause) {
        failedTests.add(new GlobalTestId(testFile, testId));
    }
}
