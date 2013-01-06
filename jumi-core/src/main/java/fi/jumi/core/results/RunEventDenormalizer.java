// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.results;

import fi.jumi.api.drivers.TestId;
import fi.jumi.core.api.*;
import fi.jumi.core.runs.RunId;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;

@NotThreadSafe
public class RunEventDenormalizer implements SuiteListener {

    private final RunVisitor visitor;
    private final Deque<TestId> runningTests = new ArrayDeque<>();
    private TestFile testFile;

    public RunEventDenormalizer(RunVisitor visitor) {
        this.visitor = visitor;
    }

    private TestId getTestId() {
        return runningTests.getFirst();
    }

    @Override
    public final void onSuiteStarted() {
        assertShouldNotBeCalled();
    }

    @Override
    public final void onTestFound(TestFile testFile, TestId testId, String name) {
        assertShouldNotBeCalled();
    }

    @Override
    public void onRunStarted(RunId runId, TestFile testFile) {
        this.testFile = testFile;
        visitor.onRunStarted(runId, testFile);
    }

    @Override
    public void onTestStarted(RunId runId, TestId testId) {
        runningTests.push(testId);
        visitor.onTestStarted(runId, testFile, testId);
    }

    @Override
    public void onPrintedOut(RunId runId, String text) {
        visitor.onPrintedOut(runId, testFile, getTestId(), text);
    }

    @Override
    public void onPrintedErr(RunId runId, String text) {
        visitor.onPrintedErr(runId, testFile, getTestId(), text);
    }

    @Override
    public void onFailure(RunId runId, StackTrace cause) {
        visitor.onFailure(runId, testFile, getTestId(), cause);
    }

    @Override
    public void onTestFinished(RunId runId) {
        visitor.onTestFinished(runId, testFile, getTestId());
        runningTests.pop();
    }

    @Override
    public void onRunFinished(RunId runId) {
        visitor.onRunFinished(runId, testFile);
        this.testFile = null;
    }

    @Override
    public final void onSuiteFinished() {
        assertShouldNotBeCalled();
    }

    private static void assertShouldNotBeCalled() {
        throw new AssertionError("should not be called; not a run-specific event");
    }
}
