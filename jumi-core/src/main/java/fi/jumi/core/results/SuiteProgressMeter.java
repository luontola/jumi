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

    private final Map<TestFile, FileState> files = new HashMap<>();
    private final Map<RunId, RunState> runs = new HashMap<>();
    private Status status = Status.UNDETERMINED;

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

    @Override
    public void onTestFileFound(TestFile testFile) {
        files.put(testFile, new FileState());
    }

    @Override
    public void onTestFileFinished(TestFile testFile) {
        files.get(testFile).onTestFileFinished();
    }

    @Override
    public void onTestFound(TestFile testFile, TestId testId, String name) {
        files.get(testFile).onTestFound(testId);
    }

    @Override
    public void onRunStarted(RunId runId, TestFile testFile) {
        runs.put(runId, new RunState(files.get(testFile)));
    }

    @Override
    public void onTestStarted(RunId runId, TestId testId) {
        runs.get(runId).onTestStarted(testId);
    }

    @Override
    public void onTestFinished(RunId runId) {
        runs.get(runId).onTestFinished();
    }

    @Override
    public void onRunFinished(RunId runId) {
        runs.remove(runId); // let memory be garbage collected
    }

    @Override
    public void onAllTestFilesFound() {
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
        private final FileState file;

        public RunState(FileState file) {
            this.file = file;
        }

        public void onTestStarted(TestId testId) {
            activeTests.push(testId);
        }

        public void onTestFinished() {
            TestId finishedTest = activeTests.pop();
            file.onTestFinished(finishedTest);
        }
    }

    @NotThreadSafe
    private static class FileState {
        private Set<TestId> allTests = new HashSet<>();
        private Set<TestId> finishedTests = new HashSet<>();
        private boolean fileFinished = false;

        public double getCompletion() {
            if (fileFinished) {
                return 1;
            }
            return allTests.size() == 0 ? 0 :
                    (double) finishedTests.size() / (double) allTests.size();
        }

        public void onTestFound(TestId testId) {
            allTests.add(testId);
        }

        private void onTestFinished(TestId testId) {
            finishedTests.add(testId);
        }

        public void onTestFileFinished() {
            fileFinished = true;

            // let memory be garbage collected
            allTests = null;
            finishedTests = null;
        }
    }
}
