// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.junit;

import fi.jumi.api.drivers.TestId;
import fi.jumi.core.api.TestFile;
import fi.jumi.core.results.*;
import fi.jumi.core.runs.RunId;
import fi.jumi.core.testbench.TestBench;
import fi.jumi.core.util.SpyListener;
import org.junit.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class JUnitCompatibilityDriverTest {

    private final TestBench testBench = new TestBench();
    private final SpyListener<RunVisitor> spy = new SpyListener<>(RunVisitor.class);
    private final RunVisitor expect = spy.getListener();

    private Class<?> testClass;
    private TestFile testFile;
    private SuiteEventDemuxer results;

    @Before
    public void setup() throws Exception {
        testBench.setDriverFinder(new JUnitCompatibilityDriverFinder());
//        testBench.setActorsMessageListener(new PrintStreamMessageLogger(System.out));
    }

    private void runTestClass(Class<?> testClass) {
        this.testClass = testClass;
        testFile = TestFile.fromClass(testClass);
        results = testBench.run(testClass);
    }

    private void checkExpectations() {
        spy.replay();
        results.visitAllRuns(expect);
        spy.verify();
    }


    @Test
    public void single_passing_test() {
        runTestClass(OnePassing.class);

        assertThat(results.getTestName(testFile, TestId.ROOT), is(testClass.getSimpleName()));
        assertThat(results.getTestName(testFile, TestId.of(0)), is("testPassing"));

        expect.onRunStarted(new RunId(1), testFile);
        expect.onTestStarted(new RunId(1), testFile, TestId.ROOT);
        expect.onTestStarted(new RunId(1), testFile, TestId.of(0));
        expect.onTestFinished(new RunId(1), testFile, TestId.of(0));
        expect.onTestFinished(new RunId(1), testFile, TestId.ROOT);
        expect.onRunFinished(new RunId(1), testFile);

        checkExpectations();
    }

    @Test
    public void multiple_passing_tests() {
        runTestClass(TwoPassing.class);

        assertThat(results.getTestName(testFile, TestId.ROOT), is(testClass.getSimpleName()));
        assertThat(results.getTestName(testFile, TestId.of(0)), is("testOne"));
        assertThat(results.getTestName(testFile, TestId.of(1)), is("testTwo"));

        expect.onRunStarted(new RunId(1), testFile);
        expect.onTestStarted(new RunId(1), testFile, TestId.ROOT);
        expect.onTestStarted(new RunId(1), testFile, TestId.of(0));
        expect.onTestFinished(new RunId(1), testFile, TestId.of(0));
        expect.onTestFinished(new RunId(1), testFile, TestId.ROOT);
        expect.onRunFinished(new RunId(1), testFile);

        expect.onRunStarted(new RunId(2), testFile);
        expect.onTestStarted(new RunId(2), testFile, TestId.ROOT);
        expect.onTestStarted(new RunId(2), testFile, TestId.of(1));
        expect.onTestFinished(new RunId(2), testFile, TestId.of(1));
        expect.onTestFinished(new RunId(2), testFile, TestId.ROOT);
        expect.onRunFinished(new RunId(2), testFile);

        checkExpectations();
    }


    // TODO: deep test hierarchies
    // TODO: failures
    // TODO: ignored tests
    // TODO: assumptions

    public static class OnePassing {
        @Test
        public void testPassing() {
        }
    }

    public static class TwoPassing {
        @Test
        public void testOne() {
        }

        @Test
        public void testTwo() {
        }
    }
}
