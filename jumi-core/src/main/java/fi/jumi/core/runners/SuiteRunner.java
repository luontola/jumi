// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.actors.*;
import fi.jumi.actors.workers.*;
import fi.jumi.api.drivers.*;
import fi.jumi.core.*;
import fi.jumi.core.drivers.DriverFinder;
import fi.jumi.core.files.*;
import fi.jumi.core.output.OutputCapturer;
import fi.jumi.core.runs.*;
import fi.jumi.core.util.ClassFiles;

import javax.annotation.concurrent.NotThreadSafe;
import java.nio.file.Path;
import java.util.concurrent.Executor;

@NotThreadSafe
public class SuiteRunner implements Startable, TestClassFinderListener {

    private final SuiteListener suiteListener;
    private final ClassLoader testClassLoader;
    private final TestClassFinder testClassFinder;
    private final DriverFinder driverFinder;
    private final ActorThread actorThread;
    private final Executor testExecutor;
    private final OutputCapturer outputCapturer;

    private final RunIdSequence runIdSequence = new RunIdSequence();
    private int childRunners = 0;

    // XXX: too many constructor parameters, could we group some of them together?
    public SuiteRunner(SuiteListener suiteListener,
                       ClassLoader testClassLoader,
                       TestClassFinder testClassFinder,
                       DriverFinder driverFinder,
                       ActorThread actorThread,
                       Executor testExecutor,
                       OutputCapturer outputCapturer) {
        this.suiteListener = suiteListener;
        this.testClassLoader = testClassLoader;
        this.testClassFinder = testClassFinder;
        this.driverFinder = driverFinder;
        this.actorThread = actorThread;
        this.testExecutor = testExecutor;
        this.outputCapturer = outputCapturer;
    }

    @Override
    public void start() {
        suiteListener.onSuiteStarted();

        TestClassFinderRunner runner = new TestClassFinderRunner(
                testClassFinder,
                actorThread.bindActor(TestClassFinderListener.class, this)
        );

        WorkerCounter executor = new WorkerCounter(testExecutor);
        executor.execute(runner);
        executor.afterPreviousWorkersFinished(childRunnerListener());
    }

    @Override
    public void onTestClassFound(Path testFile) {
        Class<?> testClass = loadTestClass(testFile);
        Driver driver = driverFinder.findTestClassDriver(testClass);

        SuiteNotifier suiteNotifier = new DefaultSuiteNotifier(
                actorThread.bindActor(TestClassListener.class,
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
