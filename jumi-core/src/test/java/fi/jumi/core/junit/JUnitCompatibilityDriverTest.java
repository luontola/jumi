// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.junit;

import fi.jumi.api.drivers.TestId;
import fi.jumi.core.api.*;
import fi.jumi.core.results.*;
import fi.jumi.core.runs.RunId;
import fi.jumi.core.testbench.TestBench;
import fi.jumi.core.util.SpyListener;
import org.junit.*;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class JUnitCompatibilityDriverTest {

    private final TestBench testBench = new TestBench();
    private final SpyListener<RunVisitor> spy = new SpyListener<>(RunVisitor.class);
    private final RunVisitor expect = spy.getListener();

    private TestFile testFile;
    private SuiteEventDemuxer results;

    @Before
    public void setup() {
        testBench.setDriverFinder(new JUnitCompatibilityDriverFinder());
//        testBench.setActorsMessageListener(new PrintStreamMessageLogger(System.out));
    }

    private void runTestClass(Class<?> testClass) {
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

        assertThat(results.getTestName(testFile, TestId.ROOT), is("OnePassing"));
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
    public void single_failing_test() {
        runTestClass(OneFailing.class);

        assertThat(results.getTestName(testFile, TestId.ROOT), is("OneFailing"));
        assertThat(results.getTestName(testFile, TestId.of(0)), is("testFailing"));

        expect.onRunStarted(new RunId(1), testFile);
        expect.onTestStarted(new RunId(1), testFile, TestId.ROOT);
        expect.onTestStarted(new RunId(1), testFile, TestId.of(0));
        expect.onFailure(new RunId(1), testFile, TestId.of(0), StackTrace.copyOf(new AssertionError("dummy failure")));
        expect.onTestFinished(new RunId(1), testFile, TestId.of(0));
        expect.onTestFinished(new RunId(1), testFile, TestId.ROOT);
        expect.onRunFinished(new RunId(1), testFile);

        checkExpectations();
    }

    @Test
    public void multiple_passing_tests() {
        runTestClass(TwoPassing.class);

        assertThat(results.getTestName(testFile, TestId.ROOT), is("TwoPassing"));
        String name0 = results.getTestName(testFile, TestId.of(0));
        String name1 = results.getTestName(testFile, TestId.of(1));
        assertThat(Arrays.asList(name0, name1), containsInAnyOrder("testOne", "testTwo")); // JUnit's order of test methods is undefined

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

    @Test
    public void deep_test_hierarchies() {
        runTestClass(DeepHierarchy.class);

        assertThat(results.getTestName(testFile, TestId.ROOT), is("DeepHierarchy"));
        assertThat(results.getTestName(testFile, TestId.of(0)), is("NestedContext"));
        assertThat(results.getTestName(testFile, TestId.of(0, 0)), is("theLeaf"));

        expect.onRunStarted(new RunId(1), testFile);
        expect.onTestStarted(new RunId(1), testFile, TestId.ROOT);
        expect.onTestStarted(new RunId(1), testFile, TestId.of(0));
        expect.onTestStarted(new RunId(1), testFile, TestId.of(0, 0));
        expect.onTestFinished(new RunId(1), testFile, TestId.of(0, 0));
        expect.onTestFinished(new RunId(1), testFile, TestId.of(0));
        expect.onTestFinished(new RunId(1), testFile, TestId.ROOT);
        expect.onRunFinished(new RunId(1), testFile);

        checkExpectations();
    }


    // TODO: report failures from JUnit test mechanism
    // TODO: ignored tests
    // TODO: assumptions

    public static class OnePassing {
        @Test
        public void testPassing() {
        }
    }

    public static class OneFailing {
        @Test
        public void testFailing() {
            throw new AssertionError("dummy failure");
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

    @RunWith(Enclosed.class)
    public static class DeepHierarchy {
        public static class NestedContext {
            @Test
            public void theLeaf() {
            }
        }
    }
}
