// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.suite;

import fi.jumi.actors.*;
import fi.jumi.actors.workers.*;
import fi.jumi.api.drivers.*;
import fi.jumi.core.api.SuiteListener;
import fi.jumi.core.discovery.*;
import fi.jumi.core.drivers.*;
import fi.jumi.core.output.OutputCapturer;
import fi.jumi.core.runs.*;
import fi.jumi.core.util.*;

import javax.annotation.concurrent.NotThreadSafe;
import java.nio.file.Path;
import java.util.concurrent.Executor;

@NotThreadSafe
public class SuiteRunner implements Startable, TestFileFinderListener {

    private final SuiteListener suiteListener;
    private final ClassLoader testClassLoader;
    private final TestFileFinder testFileFinder;
    private final DriverFinder driverFinder;
    private final ActorThread actorThread;
    private final Executor testExecutor;
    private final OutputCapturer outputCapturer;

    private final RunIdSequence runIdSequence = new RunIdSequence();
    private int childRunners = 0;

    // XXX: too many constructor parameters, could we group some of them together?
    public SuiteRunner(SuiteListener suiteListener,
                       ClassLoader testClassLoader,
                       TestFileFinder testFileFinder,
                       DriverFinder driverFinder,
                       ActorThread actorThread,
                       Executor testExecutor,
                       OutputCapturer outputCapturer) {
        this.suiteListener = suiteListener;
        this.testClassLoader = testClassLoader;
        this.testFileFinder = testFileFinder;
        this.driverFinder = driverFinder;
        this.actorThread = actorThread;
        this.testExecutor = testExecutor;
        this.outputCapturer = outputCapturer;
    }

    @Override
    public void start() {
        suiteListener.onSuiteStarted();

        TestFileFinderRunner runner = new TestFileFinderRunner(
                testFileFinder,
                actorThread.bindActor(TestFileFinderListener.class, this)
        );

        WorkerCounter executor = new WorkerCounter(testExecutor);
        executor.execute(runner);
        executor.afterPreviousWorkersFinished(childRunnerListener());
    }

    @Override
    public void onTestFileFound(Path testFile) {
        Class<?> testClass = loadTestClass(testFile);
        Driver driver = driverFinder.findTestClassDriver(testClass);

        SuiteNotifier suiteNotifier = new DefaultSuiteNotifier(
                actorThread.bindActor(RunListener.class,
                        new DuplicateOnTestFoundEventFilter(
                                new SuiteListenerAdapter(suiteListener, testClass))),
                runIdSequence,
                outputCapturer
        );

        WorkerCounter executor = new WorkerCounter(testExecutor);
        executor.execute(new DriverRunner(driver, testClass, suiteNotifier, executor));
        executor.afterPreviousWorkersFinished(childRunnerListener());
    }

    private Class<?> loadTestClass(Path testFile) {
        try {
            return testClassLoader.loadClass(ClassFiles.pathToClassName(testFile));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot load class from " + testFile, e);
        }
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
        childRunners++;
    }

    private void fireChildRunnerFinished() {
        childRunners--;
        if (childRunners == 0) {
            suiteListener.onSuiteFinished();
        }
    }
}
