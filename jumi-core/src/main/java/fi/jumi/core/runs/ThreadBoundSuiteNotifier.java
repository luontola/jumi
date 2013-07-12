// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runs;

import fi.jumi.actors.ActorRef;
import fi.jumi.api.drivers.*;
import fi.jumi.core.stdout.OutputCapturer;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class ThreadBoundSuiteNotifier implements SuiteNotifier {

    private final InheritableThreadLocal<Run> currentRun = new InheritableThreadLocal<>();

    private final ActorRef<RunListener> listener;
    private final RunIdSequence runIdSequence;
    private final OutputCapturer outputCapturer;

    public ThreadBoundSuiteNotifier(ActorRef<RunListener> listener, RunIdSequence runIdSequence, OutputCapturer outputCapturer) {
        this.listener = listener;
        this.runIdSequence = runIdSequence;
        this.outputCapturer = outputCapturer;
    }

    @Override
    public void fireTestFound(TestId testId, String name) {
        listener.tell().onTestFound(testId, name);
    }

    @Override
    public TestNotifier fireTestStarted(TestId testId) {
        Run run = this.currentRun.get();

        if (run == null || run.isRunFinished()) {
            run = new Run(listener, outputCapturer, runIdSequence.nextRunId());
            run.fireRunStarted();
            this.currentRun.set(run);
        }

        return run.fireTestStarted(testId);
    }

    @Override
    public void fireInternalError(String message, Throwable cause) {
        listener.tell().onInternalError(message, cause);
    }
}
