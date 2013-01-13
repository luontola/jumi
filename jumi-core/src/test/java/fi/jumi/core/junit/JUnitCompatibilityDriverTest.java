// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.junit;

import fi.jumi.api.drivers.TestId;
import fi.jumi.core.api.TestFile;
import fi.jumi.core.drivers.JUnitCompatibilityDriverFinder;
import fi.jumi.core.results.*;
import fi.jumi.core.runs.RunId;
import fi.jumi.core.testbench.TestBench;
import fi.jumi.core.util.SpyListener;
import junit.framework.TestCase;
import org.junit.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class JUnitCompatibilityDriverTest {

    private final TestBench testBench = new TestBench();

    @Before
    public void setup() throws Exception {
        testBench.setDriverFinder(new JUnitCompatibilityDriverFinder(getClass().getClassLoader()));
//        testBench.setActorsMessageListener(new PrintStreamMessageLogger(System.out));
    }

    @Test
    public void runs_JUnit_3_tests() { // TODO: rename test to be not JUnit 3 specific
        Class<?> testClass = OnePassingJUnit3.class;
        TestFile testFile = TestFile.fromClass(testClass);
        SuiteEventDemuxer results = testBench.run(testClass);

        assertThat(results.getTestName(testFile, TestId.ROOT), is(testClass.getSimpleName()));
        assertThat(results.getTestName(testFile, TestId.of(0)), is("testPassing"));

        SpyListener<RunVisitor> spy = new SpyListener<>(RunVisitor.class);
        RunVisitor listener = spy.getListener();

        listener.onRunStarted(new RunId(1), testFile);
        listener.onTestStarted(new RunId(1), testFile, TestId.ROOT);
        listener.onTestStarted(new RunId(1), testFile, TestId.of(0));
        listener.onTestFinished(new RunId(1), testFile, TestId.of(0));
        listener.onTestFinished(new RunId(1), testFile, TestId.ROOT);
        listener.onRunFinished(new RunId(1), testFile);

        spy.replay();
        results.visitAllRuns(listener);
        spy.verify();
    }

    // TODO: JUnit 4
    // TODO: multiple test methods
    // TODO: deep test hierarchies
    // TODO: failures
    // TODO: ignored tests
    // TODO: assumptions

    public static class OnePassingJUnit3 extends TestCase {
        public void testPassing() {
        }
    }
}
