// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.suite;

import fi.jumi.actors.ActorThread;
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
    public void onTestFileFound(final TestFile testFile) {
        suiteListener.onTestFileFound(testFile);

        @NotThreadSafe
        class FireTestFileFinished implements WorkerListener {
            @Override
            public void onAllWorkersFinished() {
                suiteListener.onTestFileFinished(testFile);
            }
        }

        WorkerCounter testFileCompletionMonitor = new WorkerCounter(new InternalErrorReportingExecutor(suiteCompletionMonitor, suiteListener, logOutput));
        testFileCompletionMonitor.execute(driverFactory.createDriverRunner(testFile, testFileCompletionMonitor));
        testFileCompletionMonitor.afterPreviousWorkersFinished(actorThread.bindActor(WorkerListener.class, new FireTestFileFinished()));
    }

    @Override
    public void onAllTestFilesFound() {
        suiteListener.onAllTestFilesFound();

        @NotThreadSafe
        class FireSuiteFinished implements WorkerListener {
            @Override
            public void onAllWorkersFinished() {
                suiteListener.onSuiteFinished();
            }
        }

        suiteCompletionMonitor.afterPreviousWorkersFinished(actorThread.bindActor(WorkerListener.class, new FireSuiteFinished()));
    }

    // TODO: If we need events about individual test files finishing, consider the design that this class had in revision 8d3526b29f378
}
