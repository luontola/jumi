// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.results;

import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.queue.MessageSender;
import fi.jumi.api.drivers.TestId;
import fi.jumi.core.api.*;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;

@NotThreadSafe
public class SuiteEventDemuxer implements MessageSender<Event<SuiteListener>> {

    private final Map<GlobalTestId, String> testNames = new HashMap<>();
    private final Map<RunId, RunState> runs = new HashMap<>();
    private final InternalDemuxer internalDemuxer = new InternalDemuxer();

    private Event<SuiteListener> currentMessage;
    private boolean suiteFinished = false;

    @Override
    public void send(Event<SuiteListener> message) {
        currentMessage = message;
        try {
            message.fireOn(internalDemuxer);
        } finally {
            currentMessage = null;
        }
    }

    private void saveCurrentMessageToRun(RunId runId) {
        RunState run = runs.get(runId);
        if (run == null) {
            run = new RunState();
            runs.put(runId, run);
        }
        run.events.add(currentMessage);
    }

    public boolean isSuiteFinished() {
        return suiteFinished;
    }

    public void visitAllRuns(RunVisitor visitor) {
        visitAllRuns(new RunEventDenormalizer(visitor));
    }

    public void visitAllRuns(SuiteListener visitor) {
        for (RunId runId : runs.keySet()) {
            visitRun(runId, visitor);
        }
    }

    public void visitRun(RunId runId, RunVisitor visitor) {
        visitRun(runId, new RunEventDenormalizer(visitor));
    }

    public void visitRun(RunId runId, SuiteListener visitor) {
        for (Event<SuiteListener> event : runs.get(runId).events) {
            event.fireOn(visitor);
        }
    }

    private void addTestName(TestFile testFile, TestId testId, String name) {
        testNames.put(new GlobalTestId(testFile, testId), name);
    }

    public String getTestName(TestFile testFile, TestId testId) {
        String name = testNames.get(new GlobalTestId(testFile, testId));
        if (name == null) {
            throw new IllegalArgumentException("name not found for " + testFile + " and " + testId);
        }
        return name;
    }


    @NotThreadSafe
    private static class RunState {
        public final List<Event<SuiteListener>> events = new ArrayList<>();
    }

    @NotThreadSafe
    private class InternalDemuxer extends NullSuiteListener {

        @Override
        public void onInternalError(String message, StackTrace cause) {
            // TODO: save the error? or ignore because this is not a run-level error?
        }

        @Override
        public void onTestFound(TestFile testFile, TestId testId, String name) {
            addTestName(testFile, testId, name);
        }

        @Override
        public void onRunStarted(RunId runId, TestFile testFile) {
            saveCurrentMessageToRun(runId);
        }

        @Override
        public void onTestStarted(RunId runId, TestId testId) {
            saveCurrentMessageToRun(runId);
        }

        @Override
        public void onPrintedOut(RunId runId, String text) {
            saveCurrentMessageToRun(runId);
        }

        @Override
        public void onPrintedErr(RunId runId, String text) {
            saveCurrentMessageToRun(runId);
        }

        @Override
        public void onFailure(RunId runId, StackTrace cause) {
            saveCurrentMessageToRun(runId);
        }

        @Override
        public void onTestFinished(RunId runId) {
            saveCurrentMessageToRun(runId);
        }

        @Override
        public void onRunFinished(RunId runId) {
            saveCurrentMessageToRun(runId);
        }

        @Override
        public void onSuiteFinished() {
            suiteFinished = true;
        }
    }
}
