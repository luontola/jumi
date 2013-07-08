// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.ui;

import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.queue.MessageReceiver;
import fi.jumi.api.drivers.TestId;
import fi.jumi.core.api.*;
import fi.jumi.core.results.*;

import javax.annotation.CheckForNull;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.*;

@NotThreadSafe
public class TextUI {

    // TODO: if multiple readers are needed, create a Streamer class per the original designs
    private final MessageReceiver<Event<SuiteListener>> eventStream;
    private final Printer printer;

    private final SuiteEventDemuxer demuxer = new SuiteEventDemuxer();
    private final SuitePrinter suitePrinter = new SuitePrinter();
    private boolean passingTestsVisible = true;
    private boolean hasInternalErrors = false;
    private boolean hasFailures = false;

    public TextUI(MessageReceiver<Event<SuiteListener>> eventStream, Printer printer) {
        this.eventStream = eventStream;
        this.printer = printer;
    }

    public void setPassingTestsVisible(boolean passingTestsVisible) {
        this.passingTestsVisible = passingTestsVisible;
    }

    public boolean hasFailures() {
        return hasFailures || hasInternalErrors;
    }

    public void update() {
        while (!demuxer.isSuiteFinished()) {
            Event<SuiteListener> message = eventStream.poll();
            if (message == null) {
                break;
            }
            updateWithMessage(message);
        }
    }

    public void updateUntilFinished() throws InterruptedException {
        while (!demuxer.isSuiteFinished()) {
            Event<SuiteListener> message = eventStream.take();
            updateWithMessage(message);
        }
    }

    private void updateWithMessage(Event<SuiteListener> message) {
        demuxer.send(message);
        message.fireOn(suitePrinter);
    }


    // printing visitors

    @NotThreadSafe
    private class SuitePrinter extends NullSuiteListener {

        @Override
        public void onInternalError(String message, StackTrace cause) {
            printer.printlnMeta(" > Internal Error");
            printer.printlnMeta(" > " + message);
            printer.printErr(getStackTraceAsString(cause));
            printer.printErr("\n");
            hasInternalErrors = true;
        }

        @Override
        public void onFailure(RunId runId, StackTrace cause) {
            hasFailures = true;
        }

        @Override
        public void onRunFinished(RunId runId) {
            if (passingTestsVisible || hasFailures(runId)) {
                demuxer.visitRun(runId, new RunPrinter());
            }
        }

        private boolean hasFailures(RunId runId) {
            SuiteResultsSummary tmp = new SuiteResultsSummary();
            demuxer.visitRun(runId, tmp);
            return tmp.getFailingTests() > 0;
        }

        @Override
        public void onSuiteFinished() {
            SuiteResultsSummary summary = new SuiteResultsSummary();
            demuxer.visitAllRuns(summary);
            printSuiteFooter(summary);
        }

        // visual style

        private void printSuiteFooter(SuiteResultsSummary summary) {
            int pass = summary.getPassingTests();
            int fail = summary.getFailingTests();
            printer.printlnMeta(String.format("Pass: %d, Fail: %d", pass, fail));
            if (hasFailures) {
                printer.printlnMeta("There were test failures");
            }
            if (hasInternalErrors) {
                printer.printlnMeta("There were internal errors");
            }
        }
    }

    @NotThreadSafe
    private class RunPrinter implements RunVisitor {

        private int testNestingLevel = 0;

        @Override
        public void onRunStarted(RunId runId, TestFile testFile) {
            printRunHeader(testFile, runId);
        }

        @Override
        public void onTestStarted(RunId runId, TestFile testFile, TestId testId) {
            testNestingLevel++;
            printTestName("+", testFile, testId);
        }

        @Override
        public void onPrintedOut(RunId runId, TestFile testFile, @CheckForNull TestId testId, String text) {
            printer.printOut(text);
        }

        @Override
        public void onPrintedErr(RunId runId, TestFile testFile, @CheckForNull TestId testId, String text) {
            printer.printErr(text);
        }

        @Override
        public void onFailure(RunId runId, TestFile testFile, TestId testId, StackTrace cause) {
            printer.printErr(getStackTraceAsString(cause));
        }

        @Override
        public void onTestFinished(RunId runId, TestFile testFile, TestId testId) {
            printTestName("-", testFile, testId);
            testNestingLevel--;
        }

        @Override
        public void onRunFinished(RunId runId, TestFile testFile) {
            printRunFooter();
        }

        // visual style

        private void printRunHeader(TestFile testFile, RunId runId) {
            printer.printlnMeta(" > Run #" + runId.toInt() + " in " + testFile);
        }

        private void printTestName(String bullet, TestFile testFile, TestId testId) {
            printer.printlnMeta(" > " + testNameIndent() + bullet + " " + demuxer.getTestName(testFile, testId));
        }

        private void printRunFooter() {
            printer.printlnMeta("");
        }

        private String testNameIndent() {
            StringBuilder indent = new StringBuilder();
            for (int i = 1; i < testNestingLevel; i++) {
                indent.append("  ");
            }
            return indent.toString();
        }
    }

    private static String getStackTraceAsString(Throwable cause) {
        StringWriter buffer = new StringWriter();
        cause.printStackTrace(new PrintWriter(buffer));
        return buffer.toString();
    }
}
