// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runs;

import fi.jumi.actors.ActorRef;
import fi.jumi.api.drivers.*;
import fi.jumi.core.output.*;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
class CurrentRun {

    private final ActorRef<RunListener> listener;
    private final OutputCapturer outputCapturer;

    private final RunId runId;
    private volatile Test innermostNonFinishedTest = null;

    public CurrentRun(ActorRef<RunListener> listener, OutputCapturer outputCapturer, RunId runId) {
        this.listener = listener;
        this.outputCapturer = outputCapturer;
        this.runId = runId;
    }

    public boolean isRunFinished() {
        return innermostNonFinishedTest == null;
    }

    public void fireRunStarted() {
        listener.tell().onRunStarted(runId);
        outputCapturer.captureTo(new OutputListenerAdapter(listener, runId));
    }

    private void fireRunFinished() {
        outputCapturer.captureTo(new NullOutputListener());
        listener.tell().onRunFinished(runId);
    }

    public TestNotifier fireTestStarted(TestId testId) {
        listener.tell().onTestStarted(runId, testId);

        Test test = new Test(testId, innermostNonFinishedTest);
        innermostNonFinishedTest = test;
        return test;
    }


    @ThreadSafe
    private class Test implements TestNotifier {

        private final TestId testId;
        private final Test enclosingTest;
        private volatile boolean testFinished = false;

        public Test(TestId testId, Test enclosingTest) {
            this.testId = testId;
            this.enclosingTest = enclosingTest;
        }

        @Override
        public void fireFailure(Throwable cause) {
            checkInnermostNonFinishedTest();
            listener.tell().onFailure(runId, testId, cause);
        }

        @Override
        public void fireTestFinished() {
            if (testFinished) {
                throw new IllegalStateException("cannot be called multiple times; " + testId + " is already finished");
            }

            checkInnermostNonFinishedTest();
            testFinished = true; // TODO: remove me?
            innermostNonFinishedTest = enclosingTest;
            listener.tell().onTestFinished(runId, testId);

            if (isRunFinished()) {
                fireRunFinished();
            }
        }

        private void checkInnermostNonFinishedTest() {
            if (innermostNonFinishedTest != this) {
                throw new IllegalStateException("must be called on the innermost non-finished TestNotifier; " +
                        "expected " + innermostNonFinishedTest + " but was " + this);
            }
        }

        @Override
        public String toString() {
            return testId.toString(); // TODO: our own toString
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
