// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.suite;

import fi.jumi.actors.*;
import fi.jumi.actors.workers.*;
import fi.jumi.core.api.*;
import fi.jumi.core.discovery.TestFileFinderListener;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.PrintStream;
import java.util.concurrent.Executor;

@NotThreadSafe
public class SuiteRunner implements TestFileFinderListener {

    private final DriverFactory driverFactory;

    private final SuiteListener suiteListener;
    private final ActorThread actorThread;
    private final Executor testExecutor;
    private final PrintStream logOutput;

    private int activeTestFiles = 0;
    private boolean allTestFilesStarted;

    // XXX: too many constructor parameters, could we group some of them together?
    public SuiteRunner(DriverFactory driverFactory,
                       SuiteListener suiteListener,
                       ActorThread actorThread,
                       Executor testExecutor,
                       PrintStream logOutput) {
        this.driverFactory = driverFactory;
        this.suiteListener = suiteListener;
        this.actorThread = actorThread;
        this.testExecutor = testExecutor;
        this.logOutput = logOutput;
    }

    @Override
    public void onTestFileFound(TestFile testFile) {
        WorkerCounter workerCounter = new WorkerCounter(testExecutor);
        Executor executor = new InternalErrorReportingExecutor(workerCounter, suiteListener, logOutput);
        executor.execute(driverFactory.createDriverRunner(testFile, executor));
        workerCounter.afterPreviousWorkersFinished(childRunnerListener());
    }

    @Override
    public void onAllTestFilesFound() {
        allTestFilesStarted = true;
        checkSuiteFinished();
    }

    private ActorRef<WorkerListener> childRunnerListener() {
        fireChildRunnerStarted();

        @NotThreadSafe
        class OnRunnerFinished implements WorkerListener {
            @Override
            public void onAllWorkersFinished() {
                fireChildRunnerFinished();
            }
        }
        return actorThread.bindActor(WorkerListener.class, new OnRunnerFinished());
    }

    private void fireChildRunnerStarted() {
        activeTestFiles++;
    }

    private void fireChildRunnerFinished() {
        activeTestFiles--;
        checkSuiteFinished();
    }

    private void checkSuiteFinished() {
        if (allTestFilesStarted && activeTestFiles == 0) {
            suiteListener.onSuiteFinished();
        }
    }
}
