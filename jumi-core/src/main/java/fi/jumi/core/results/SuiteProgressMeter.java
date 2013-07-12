// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.results;

import fi.jumi.api.drivers.TestId;
import fi.jumi.core.api.*;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;

@NotThreadSafe
public class SuiteProgressMeter extends NullSuiteListener {

    private Status status = Status.UNDETERMINED;
    private int totalFiles = 0;
    private int finishedFiles = 0;

    private Set<GlobalTestId> allTests = new HashSet<>();
    private Set<GlobalTestId> finishedTests = new HashSet<>();
    private Map<RunId, RunState> activeRuns = new HashMap<>();

    public double getCompletion() {
        if (status == Status.UNDETERMINED) {
            return 0;
        }
        if (totalFiles == 0) {
            return 1;
        }
        double files = (double) finishedFiles / (double) totalFiles;

        double tests = (double) finishedTests.size() / (double) allTests.size();
        if (allTests.size() == 0) {
            tests = 0;
        }

        return Math.max(files, tests);
    }

    public Status getStatus() {
        return status;
    }


    // suite events

    public void onTestFileFound(TestFile testFile) { // TODO: add this method to SuiteListener
        totalFiles++;
    }

    public void onTestFileFinished(TestFile testFile) { // TODO: add this method to SuiteListener
        finishedFiles++;
    }

    @Override
    public void onTestFound(TestFile testFile, TestId testId, String name) {
        allTests.add(new GlobalTestId(testFile, testId));
    }

    @Override
    public void onRunStarted(RunId runId, TestFile testFile) {
        activeRuns.put(runId, new RunState(testFile));
    }

    @Override
    public void onTestStarted(RunId runId, TestId testId) {
        RunState run = activeRuns.get(runId);
        run.onTestStarted(testId);
    }

    @Override
    public void onTestFinished(RunId runId) {
        RunState run = activeRuns.get(runId);
        finishedTests.add(run.currentTest());
        run.onTestFinished();
    }

    public void onAllTestFilesFound() { // TODO: add this method to SuiteListener
        assert status == Status.UNDETERMINED : "status was " + status;
        status = Status.IN_PROGRESS;
    }

    @Override
    public void onSuiteFinished() {
        assert status == Status.IN_PROGRESS : "status was " + status;
        status = Status.FINISHED;
    }


    public enum Status {
        UNDETERMINED,
        IN_PROGRESS,
        FINISHED
    }

    private class RunState {
        private final Deque<TestId> activeTests = new ArrayDeque<>();
        private final TestFile testFile;

        public RunState(TestFile testFile) {
            this.testFile = testFile;
        }

        public GlobalTestId currentTest() {
            return new GlobalTestId(testFile, activeTests.getFirst());
        }

        public void onTestStarted(TestId testId) {
            activeTests.push(testId);
        }

        public void onTestFinished() {
            activeTests.pop();
        }
    }
}
