// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.testbench;

import fi.jumi.actors.*;
import fi.jumi.actors.eventizers.dynamic.DynamicEventizerProvider;
import fi.jumi.actors.listeners.*;
import fi.jumi.core.api.SuiteListener;
import fi.jumi.core.discovery.*;
import fi.jumi.core.drivers.*;
import fi.jumi.core.events.SuiteListenerEventizer;
import fi.jumi.core.output.OutputCapturer;
import fi.jumi.core.results.SuiteEventDemuxer;
import fi.jumi.core.runs.RunIdSequence;
import fi.jumi.core.suite.*;
import org.apache.commons.io.output.NullOutputStream;

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
        run(new SuiteListenerEventizer().newFrontend(results), testClasses);
        return results;
    }

    public void run(SuiteListener suiteListener, Class<?>... testClasses) {
        SingleThreadedActors actors = new SingleThreadedActors(
                new DynamicEventizerProvider(),
                actorsFailureHandler,
                actorsMessageListener
        );
        ActorThread actorThread = actors.startActorThread();
        Executor testExecutor = actors.getExecutor();

        RunIdSequence runIdSequence = new RunIdSequence();
        ClassLoader classLoader = getClass().getClassLoader();

        ActorRef<TestFileFinderListener> suiteRunner = actorThread.bindActor(TestFileFinderListener.class,
                new SuiteRunner(
                        new DriverFactory(suiteListener, actorThread, outputCapturer, driverFinder, runIdSequence, classLoader),
                        suiteListener,
                        actorThread,
                        testExecutor,
                        new PrintStream(new NullOutputStream())
                ));

        suiteListener.onSuiteStarted();
        testExecutor.execute(new TestFileFinderRunner(new StubTestFileFinder(testClasses), suiteRunner));
        actors.processEventsUntilIdle();
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
