// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.junit;

import fi.jumi.actors.ActorRef;
import fi.jumi.api.drivers.TestId;
import fi.jumi.core.output.OutputCapturer;
import fi.jumi.core.runs.*;
import fi.jumi.core.util.SpyListener;
import org.junit.Test;
import org.junit.runner.Description;

public class JUnitRunListenerAdapterTest {

    private final SpyListener<RunListener> spy = new SpyListener<>(RunListener.class);
    private final RunListener expect = spy.getListener();

    private final JUnitRunListenerAdapter adapter = new JUnitRunListenerAdapter(new DefaultSuiteNotifier(ActorRef.wrap(expect), new RunIdSequence(), new OutputCapturer()));

    @Test
    public void tests_discovered_after_starting_a_run_when_root_description_is_updated() throws Exception {
        expect.onTestFound(TestId.ROOT, "DummyTest");
        expect.onTestFound(TestId.of(0), "testOne");
        expect.onTestFound(TestId.of(1), "testTwo");
        expect.onRunStarted(new RunId(1));
        expect.onTestStarted(new RunId(1), TestId.ROOT);
        expect.onTestStarted(new RunId(1), TestId.of(1));
        expect.onTestFinished(new RunId(1), TestId.of(1));
        expect.onTestFinished(new RunId(1), TestId.ROOT);
        expect.onRunFinished(new RunId(1));

        spy.replay();

        Description suite = Description.createSuiteDescription(DummyTest.class);
        Description testOne = Description.createTestDescription(DummyTest.class, "testOne");
        suite.addChild(testOne);
        adapter.testRunStarted(suite);
        Description testTwo = Description.createTestDescription(DummyTest.class, "testTwo");
        suite.addChild(testTwo);
        adapter.testStarted(testTwo);
        adapter.testFinished(testTwo);

        spy.verify();
    }

    @Test
    public void tests_discovered_after_starting_a_run_but_without_root_description_updated() throws Exception {
        expect.onTestFound(TestId.ROOT, "DummyTest");
        expect.onTestFound(TestId.of(0), "testOne");
        expect.onTestFound(TestId.of(1), "testTwo");
        expect.onRunStarted(new RunId(1));
        expect.onTestStarted(new RunId(1), TestId.ROOT);
        expect.onTestStarted(new RunId(1), TestId.of(1));
        expect.onTestFinished(new RunId(1), TestId.of(1));
        expect.onTestFinished(new RunId(1), TestId.ROOT);
        expect.onRunFinished(new RunId(1));

        spy.replay();

        Description suite = Description.createSuiteDescription(DummyTest.class);
        Description testOne = Description.createTestDescription(DummyTest.class, "testOne");
        suite.addChild(testOne);
        adapter.testRunStarted(suite);
        Description testTwo = Description.createTestDescription(DummyTest.class, "testTwo");
        // no call to `suite.addChild()`
        adapter.testStarted(testTwo);
        adapter.testFinished(testTwo);

        spy.verify();
    }


    // guinea pigs

    private static class DummyTest {
    }
}
