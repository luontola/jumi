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

    // TODO: if SuiteRunner starts containing interesting state, update DaemonProcessTest to require a custom toString() from it
    private final DriverFactory driverFactory;
    private final SuiteListener suiteListener;
    private final ActorThread actorThread;
    private final PrintStream logOutput;
    private final WorkerCounter suiteCompletionMonitor;

    // XXX: too many constructor parameters, could we group some of them together?
    public SuiteRunner(DriverFactory driverFactory,
                       SuiteListener suiteListener,
                       ActorThread actorThread,
                       Executor testExecutor,
                       PrintStream logOutput) {
        this.driverFactory = driverFactory;
        this.suiteListener = suiteListener;
        this.actorThread = actorThread;
        this.logOutput = logOutput;
        this.suiteCompletionMonitor = new WorkerCounter(testExecutor);
    }

    @Override
    public void onTestFileFound(TestFile testFile) {
        suiteListener.onTestFileFound(testFile);

        @NotThreadSafe
        class FireTestFileFinished implements WorkerListener {
            // not lambda to show up better in actor logs
            @Override
            public void onAllWorkersFinished() {
                suiteListener.onTestFileFinished(testFile);
            }

            @Override
            public String toString() {
                return getClass().getName() + "(" + testFile + ")";
            }
        }

        WorkerCounter testFileCompletionMonitor = new WorkerCounter(new InternalErrorReportingExecutor(suiteCompletionMonitor, suiteListener, logOutput));
        testFileCompletionMonitor.execute(driverFactory.createDriverRunner(testFile, testFileCompletionMonitor));
        testFileCompletionMonitor.afterPreviousWorkersFinished(asActor(new FireTestFileFinished()));
    }

    @Override
    public void onAllTestFilesFound() {
        suiteListener.onAllTestFilesFound();

        @NotThreadSafe
        class FireSuiteFinished implements WorkerListener {
            // not lambda to show up better in actor logs
            @Override
            public void onAllWorkersFinished() {
                suiteListener.onSuiteFinished();
            }
        }

        suiteCompletionMonitor.afterPreviousWorkersFinished(asActor(new FireSuiteFinished()));
    }

    private ActorRef<WorkerListener> asActor(WorkerListener rawActor) {
        return actorThread.bindActor(WorkerListener.class, rawActor);
    }
}
