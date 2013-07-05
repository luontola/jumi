// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runs;

import fi.jumi.actors.ActorRef;
import fi.jumi.api.drivers.TestId;
import fi.jumi.core.output.*;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

@ThreadSafe
class CurrentRun {

    private final ActorRef<RunListener> listener;
    private final OutputCapturer outputCapturer;

    private final RunId runId;
    private final Deque<TestId> activeTestsStack = new ConcurrentLinkedDeque<>();

    public CurrentRun(ActorRef<RunListener> listener, OutputCapturer outputCapturer, RunId runId) {
        this.listener = listener;
        this.outputCapturer = outputCapturer;
        this.runId = runId;
    }

    public boolean isRunFinished() {
        return activeTestsStack.isEmpty();
    }

    public void fireRunStarted() {
        listener.tell().onRunStarted(runId);
        outputCapturer.captureTo(new OutputListenerAdapter(listener, runId));
    }

    public void fireRunFinished() {
        outputCapturer.captureTo(new NullOutputListener());
        listener.tell().onRunFinished(runId);
    }

    public void fireTestStarted(TestId testId) {
        activeTestsStack.push(testId);
        listener.tell().onTestStarted(runId, testId);
    }

    public void fireTestFinished(TestId testId) {
        checkInnermostNonFinishedTest(testId);
        activeTestsStack.pop();
        listener.tell().onTestFinished(runId, testId);
    }

    public void fireFailure(TestId testId, Throwable cause) {
        checkInnermostNonFinishedTest(testId);
        listener.tell().onFailure(runId, testId, cause);
    }

    private void checkInnermostNonFinishedTest(TestId testId) {
        TestId innermost = activeTestsStack.peek();
        if (!innermost.equals(testId)) {
            throw new IllegalStateException("must be called on the innermost non-finished TestNotifier; " +
                    "expected " + innermost + " but was " + testId);
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
