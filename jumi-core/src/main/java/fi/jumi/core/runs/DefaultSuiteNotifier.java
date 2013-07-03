// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runs;

import fi.jumi.actors.ActorRef;
import fi.jumi.api.drivers.*;
import fi.jumi.core.output.OutputCapturer;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class DefaultSuiteNotifier implements SuiteNotifier {

    private final InheritableThreadLocal<CurrentRun> currentRun = new InheritableThreadLocal<>();

    private final ActorRef<RunListener> listener;
    private final RunIdSequence runIdSequence;
    private final OutputCapturer outputCapturer;

    public DefaultSuiteNotifier(ActorRef<RunListener> listener, RunIdSequence runIdSequence, OutputCapturer outputCapturer) {
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
        CurrentRun currentRun = this.currentRun.get();

        if (currentRun == null || currentRun.isRunFinished()) {
            currentRun = new CurrentRun(listener, outputCapturer, runIdSequence.nextRunId());
            currentRun.fireRunStarted();
            this.currentRun.set(currentRun);
        }

        currentRun.fireTestStarted(testId);
        return new DefaultTestNotifier(currentRun, testId);
    }

    @Override
    public void fireInternalError(String message, Throwable cause) {
        listener.tell().onInternalError(message, cause);
    }
}
