// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.results;

import fi.jumi.api.drivers.TestId;
import fi.jumi.core.api.*;
import fi.jumi.core.runs.RunId;

import javax.annotation.CheckForNull;
import javax.annotation.concurrent.Immutable;

@Immutable
public class NullRunVisitor implements RunVisitor {

    @Override
    public void onRunStarted(RunId runId, TestFile testFile) {
    }

    @Override
    public void onTestStarted(RunId runId, TestFile testFile, TestId testId) {
    }

    @Override
    public void onPrintedOut(RunId runId, TestFile testFile, @CheckForNull TestId testId, String text) {
    }

    @Override
    public void onPrintedErr(RunId runId, TestFile testFile, @CheckForNull TestId testId, String text) {
    }

    @Override
    public void onFailure(RunId runId, TestFile testFile, TestId testId, StackTrace cause) {
    }

    @Override
    public void onTestFinished(RunId runId, TestFile testFile, TestId testId) {
    }

    @Override
    public void onRunFinished(RunId runId, TestFile testFile) {
    }
}
