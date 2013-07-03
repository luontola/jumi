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
    private final RunIdSequence runIdSequence;
    private final OutputCapturer outputCapturer;

    private final InheritableThreadLocal<RunContext> currentRun = new InheritableThreadLocal<>();

    public CurrentRun(ActorRef<RunListener> listener, RunIdSequence runIdSequence, OutputCapturer outputCapturer) {
        this.listener = listener;
        this.runIdSequence = runIdSequence;
        this.outputCapturer = outputCapturer;
    }

    public void fireTestFound(TestId testId, String name) {
        listener.tell().onTestFound(testId, name);
    }

    public void fireTestStarted(TestId testId) {
        RunContext currentRun = this.currentRun.get();

        // notify run started?
        if (currentRun == null) {
            currentRun = new RunContext(runIdSequence.nextRunId());
            this.currentRun.set(currentRun);
            fireRunStarted(currentRun);
        }

        // notify test started
        currentRun.countTestStarted(testId);
        listener.tell().onTestStarted(currentRun.runId, testId);
    }

    public void fireTestFinished(TestId testId) {
        RunContext currentRun = this.currentRun.get();

        // notify test finished
        currentRun.countTestFinished(testId);
        listener.tell().onTestFinished(currentRun.runId, testId);

        // notify run finished?
        if (currentRun.isRunFinished()) {
            this.currentRun.remove();
            fireRunFinished(currentRun);
        }
    }

    private void fireRunStarted(RunContext currentRun) {
        listener.tell().onRunStarted(currentRun.runId);
        outputCapturer.captureTo(new OutputListenerAdapter(listener, currentRun.runId));
    }

    private void fireRunFinished(RunContext currentRun) {
        outputCapturer.captureTo(new NullOutputListener());
        listener.tell().onRunFinished(currentRun.runId);
    }

    public void fireFailure(TestId testId, Throwable cause) {
        RunContext currentRun = this.currentRun.get();
        listener.tell().onFailure(currentRun.runId, testId, cause);
    }

    public void fireInternalError(String message, Throwable cause) {
        listener.tell().onInternalError(message, cause);
    }


    @ThreadSafe
    private static class RunContext {
        public final RunId runId;
        private final Deque<TestId> activeTestsStack = new ConcurrentLinkedDeque<>();

        public RunContext(RunId runId) {
            this.runId = runId;
        }

        public void countTestStarted(TestId testId) {
            activeTestsStack.push(testId);
        }

        public void countTestFinished(TestId testId) {
            TestId innermost = activeTestsStack.peek();
            if (!innermost.equals(testId)) {
                throw new IllegalStateException("must be called on the innermost non-finished TestNotifier; " +
                        "expected " + innermost + " but was " + testId);
            }
            activeTestsStack.pop();
        }

        public boolean isRunFinished() {
            return activeTestsStack.isEmpty();
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
