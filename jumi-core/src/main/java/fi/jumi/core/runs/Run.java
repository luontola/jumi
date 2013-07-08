// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runs;

import fi.jumi.actors.ActorRef;
import fi.jumi.api.drivers.*;
import fi.jumi.core.output.*;

import javax.annotation.concurrent.ThreadSafe;
import java.util.*;

/**
 * Checks that the notifier API is used correctly. Those checks that are not convenient to do here, are done in {@link
 * RunEventNormalizer}.
 */
@ThreadSafe
class Run {

    private final ActorRef<RunListener> listener;
    private final OutputCapturer outputCapturer;

    private final RunId runId;
    /**
     * There is a race condition if {@link #fireTestStarted} or {@link Test#fireTestFinished()} is called concurrently
     * within a single {@code Run}, but it shouldn't be dangerous. A testing framework must not call those methods
     * concurrently, but if somebody anyways calls them, our error checking should notice it over 90% of the time, which
     * should be adequate for the testing framework developer to notice his mistake and fix it.
     */
    private volatile Test currentTest = null;

    public Run(ActorRef<RunListener> listener, OutputCapturer outputCapturer, RunId runId) {
        this.listener = listener;
        this.outputCapturer = outputCapturer;
        this.runId = runId;
    }

    public void fireRunStarted() {
        listener.tell().onRunStarted(runId);
        outputCapturer.captureTo(new OutputListenerAdapter(listener, runId));
    }

    private void fireRunFinished() {
        outputCapturer.captureTo(new NullOutputListener());
        listener.tell().onRunFinished(runId);
    }

    public boolean isRunFinished() {
        return currentTest == null;
    }

    public TestNotifier fireTestStarted(TestId testId) {
        listener.tell().onTestStarted(runId, testId);

        Test test = new Test(testId, currentTest);
        currentTest = test;
        return test;
    }


    @ThreadSafe
    private class Test implements TestNotifier {

        private final TestId testId;
        private final Test parent;

        public Test(TestId testId, Test parent) {
            this.testId = testId;
            this.parent = parent;
        }

        @Override
        public void fireFailure(Throwable cause) {
            checkInnermostNonFinishedTest(cause);
            listener.tell().onFailure(runId, testId, cause);
        }

        @Override
        public void fireTestFinished() {
            checkInnermostNonFinishedTest(null);
            listener.tell().onTestFinished(runId, testId);
            currentTest = parent;

            if (isRunFinished()) {
                fireRunFinished();
            }
        }

        private void checkInnermostNonFinishedTest(Throwable testFailureBeingReported) {
            if (currentTest != this) {
                String message = "must be called on the innermost non-finished TestNotifier; " +
                        "expected " + currentTest + " but was " + this + " " +
                        "which " + (isTestFinished() ? "is finished" : "is not innermost");
                IllegalStateException e = new IllegalStateException(message, testFailureBeingReported);
                listener.tell().onInternalError("Incorrect notifier API usage", e);
                throw e;
            }
        }

        private boolean isTestFinished() {
            for (Test activeTest = currentTest; activeTest != null; activeTest = activeTest.parent) {
                if (this == activeTest) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public String toString() {
            Deque<TestId> activeTestsStack = new ArrayDeque<>();
            for (Test t = this; t != null; t = t.parent) {
                activeTestsStack.push(t.testId);
            }
            return "TestNotifier(" + runId + ", " + activeTestsStack + ")";
        }
    }

    @ThreadSafe
    private static class OutputListenerAdapter implements OutputListener {
        private final ActorRef<RunListener> listener;
        private final RunId runId;

        public OutputListenerAdapter(ActorRef<RunListener> listener, RunId runId) {
            this.listener = listener;
            this.runId = runId;
        }

        @Override
        public void out(String text) {
            listener.tell().onPrintedOut(runId, text);
        }

        @Override
        public void err(String text) {
            listener.tell().onPrintedErr(runId, text);
        }
    }
}
