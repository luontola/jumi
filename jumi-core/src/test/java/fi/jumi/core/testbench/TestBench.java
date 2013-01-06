// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.testbench;

import fi.jumi.actors.*;
import fi.jumi.actors.eventizers.dynamic.DynamicEventizerProvider;
import fi.jumi.actors.listeners.*;
import fi.jumi.core.api.SuiteListener;
import fi.jumi.core.drivers.*;
import fi.jumi.core.events.SuiteListenerEventizer;
import fi.jumi.core.output.OutputCapturer;
import fi.jumi.core.results.SuiteEventDemuxer;
import fi.jumi.core.runs.RunIdSequence;
import fi.jumi.core.suite.*;

import java.io.PrintStream;
import java.util.concurrent.Executor;

public class TestBench {

    /**
     * Simulates {@link System#out}
     */
    public final PrintStream out;

    /**
     * Simulates {@link System#err}
     */
    public final PrintStream err;

    private final OutputCapturer outputCapturer;
    private DriverFinder driverFinder = new RunViaAnnotationDriverFinder();
    private MessageListener actorsMessageListener = new NullMessageListener();
    private FailureHandler actorsFailureHandler = new CrashEarlyFailureHandler();

    public TestBench() {
        outputCapturer = new OutputCapturer();
        out = outputCapturer.out();
        err = outputCapturer.err();
    }

    public SuiteEventDemuxer run(Class<?>... testClasses) {
        SuiteEventDemuxer results = new SuiteEventDemuxer();

        SingleThreadedActors actors = new SingleThreadedActors(
                new DynamicEventizerProvider(),
                actorsFailureHandler,
                actorsMessageListener
        );
        ActorThread actorThread = actors.startActorThread();
        Executor testExecutor = actors.getExecutor();

        SuiteListener suiteListener = new SuiteListenerEventizer().newFrontend(results);
        RunIdSequence runIdSequence = new RunIdSequence();
        ClassLoader classLoader = getClass().getClassLoader();

        SuiteRunner runner = new SuiteRunner(
                new DriverFactory(suiteListener, actorThread, outputCapturer, driverFinder, runIdSequence, classLoader),
                suiteListener,
                new StubTestFileFinder(testClasses),
                actorThread,
                testExecutor
        );
        runner.start();
        actors.processEventsUntilIdle();

        return results;
    }


    // setters for changing the defaults

    public void setDriverFinder(DriverFinder driverFinder) {
        this.driverFinder = driverFinder;
    }

    public void setActorsMessageListener(MessageListener actorsMessageListener) {
        this.actorsMessageListener = actorsMessageListener;
    }

    public void setActorsFailureHandler(FailureHandler actorsFailureHandler) {
        this.actorsFailureHandler = actorsFailureHandler;
    }
}
