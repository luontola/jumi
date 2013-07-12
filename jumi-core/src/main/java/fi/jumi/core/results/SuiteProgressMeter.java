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

    private Map<RunId, RunState> runs = new HashMap<>();
    private Map<TestFile, FileState> files = new HashMap<>();

    public double getCompletion() {
        if (status == Status.UNDETERMINED) {
            return 0;
        }
        double sum = 0;
        for (FileState file : files.values()) {
            sum += file.getCompletion();
        }
        return files.size() == 0 ? 1 :
                sum / files.size();
    }

    public Status getStatus() {
        return status;
    }


    // suite events

    public void onTestFileFound(TestFile testFile) { // TODO: add this method to SuiteListener
        files.put(testFile, new FileState());
    }

    public void onTestFileFinished(TestFile testFile) { // TODO: add this method to SuiteListener
        FileState file = files.get(testFile);
        file.onTestFileFinished();
    }

    @Override
    public void onTestFound(TestFile testFile, TestId testId, String name) {
        FileState file = files.get(testFile);
        file.allTests.add(testId);
    }

    @Override
    public void onRunStarted(RunId runId, TestFile testFile) {
        runs.put(runId, new RunState(testFile));
    }

    @Override
    public void onTestStarted(RunId runId, TestId testId) {
        RunState run = runs.get(runId);
        run.onTestStarted(testId);
    }

    @Override
    public void onTestFinished(RunId runId) {
        RunState run = runs.get(runId);
        FileState file = files.get(run.testFile);
        file.finishedTests.add(run.currentTest());
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


    @NotThreadSafe
    public enum Status {
        UNDETERMINED,
        IN_PROGRESS,
        FINISHED
    }

    @NotThreadSafe
    private static class RunState {
        private final Deque<TestId> activeTests = new ArrayDeque<>();
        private final TestFile testFile;

        public RunState(TestFile testFile) {
            this.testFile = testFile;
        }

        public TestId currentTest() {
            return activeTests.getFirst();
        }

        public void onTestStarted(TestId testId) {
            activeTests.push(testId);
        }

        public void onTestFinished() {
            activeTests.pop();
        }
    }

    @NotThreadSafe
    private static class FileState {
        private Set<TestId> allTests = new HashSet<>();
        private Set<TestId> finishedTests = new HashSet<>();
        private double fileFinished = 0;

        public double getCompletion() {
            double tests = allTests.size() == 0 ? 0 :
                    (double) finishedTests.size() / (double) allTests.size();
            return Math.max(tests, fileFinished);
        }

        public void onTestFileFinished() {
            fileFinished = 1;
        }
    }
}
