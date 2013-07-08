// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.results;

import fi.jumi.api.drivers.TestId;
import fi.jumi.core.api.*;

import javax.annotation.CheckForNull;

public interface RunVisitor {

    void onRunStarted(RunId runId, TestFile testFile);

    void onTestStarted(RunId runId, TestFile testFile, TestId testId);

    /**
     * @param testId null if the test continued printing after the test run finished.
     */
    void onPrintedOut(RunId runId, TestFile testFile, @CheckForNull TestId testId, String text);

    /**
     * @param testId null if the test continued printing after the test run finished.
     */
    void onPrintedErr(RunId runId, TestFile testFile, @CheckForNull TestId testId, String text);

    void onFailure(RunId runId, TestFile testFile, TestId testId, StackTrace cause);

    void onTestFinished(RunId runId, TestFile testFile, TestId testId);

    void onRunFinished(RunId runId, TestFile testFile);
}
