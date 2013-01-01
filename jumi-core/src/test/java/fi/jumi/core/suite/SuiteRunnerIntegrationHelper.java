// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.suite;

import fi.jumi.actors.*;
import fi.jumi.actors.eventizers.dynamic.DynamicEventizerProvider;
import fi.jumi.actors.listeners.*;
import fi.jumi.api.drivers.Driver;
import fi.jumi.core.api.SuiteListener;
import fi.jumi.core.discovery.TestFileFinder;
import fi.jumi.core.drivers.DriverFinder;
import fi.jumi.core.output.OutputCapturer;
import fi.jumi.core.testbench.*;
import fi.jumi.core.util.*;

import java.io.PrintStream;
import java.util.concurrent.Executor;

public abstract class SuiteRunnerIntegrationHelper {

    // TODO: replace with TestBench?

    private final SpyListener<SuiteListener> spy = new SpyListener<>(SuiteListener.class);
    protected final SuiteListener expect = spy.getListener();

    private final FailureHandler failureHandler = new CrashEarlyFailureHandler();
    private final MessageListener messageListener = new NullMessageListener();
    private final SingleThreadedActors actors = new SingleThreadedActors(new DynamicEventizerProvider(), failureHandler, messageListener);
    private final Executor executor = actors.getExecutor();

    private final OutputCapturer outputCapturer = new OutputCapturer();
    protected final PrintStream stdout = outputCapturer.out();
    protected final PrintStream stderr = outputCapturer.err();

    protected void runAndCheckExpectations(Driver driver, Class<?>... testClasses) {
        spy.replay();
        run(driver, testClasses);
        spy.verify();
    }

    protected void run(Driver driver, Class<?>... testClasses) {
        run(new StubDriverFinder(driver), testClasses);
    }

    protected void run(DriverFinder driverFinder, Class<?>... testClasses) {
        run(expect, driverFinder, testClasses);
    }

    protected void run(SuiteListener listener, Driver driver, Class<?>... testClasses) {
        run(listener, new StubDriverFinder(driver), testClasses);
    }

    protected void run(SuiteListener listener, DriverFinder driverFinder, Class<?>... testClasses) {
        TestFileFinder testFileFinder = new StubTestFileFinder(testClasses);
        ActorThread actorThread = actors.startActorThread();
        ActorRef<Startable> runner = actorThread.bindActor(Startable.class,
                new SuiteRunner(listener, getClass().getClassLoader(), testFileFinder, driverFinder, actorThread, executor, outputCapturer));
        runner.tell().start();
        actors.processEventsUntilIdle();
    }
}
