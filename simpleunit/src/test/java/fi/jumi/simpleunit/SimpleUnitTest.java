// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.simpleunit;

import fi.jumi.actors.ActorRef;
import fi.jumi.api.RunVia;
import fi.jumi.api.drivers.*;
import fi.jumi.core.api.*;
import fi.jumi.core.drivers.DriverRunner;
import fi.jumi.core.output.OutputCapturer;
import fi.jumi.core.results.*;
import fi.jumi.core.runs.*;
import fi.jumi.core.testbench.TestBench;
import fi.jumi.core.util.SpyListener;
import org.junit.Test;

import java.util.concurrent.Executor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class SimpleUnitTest {

    private static final RunId RUN_1 = new RunId(1);

    private final TestBench testBench = new TestBench();

    // TODO: think of a high-level API to write tests against, so that it hides Jumi's low-level event protocol

    @Test
    public void the_test_class_is_named_after_its_simple_name() {
        Class<OnePassingTest> testClass = OnePassingTest.class;

        SuiteEventDemuxer results = testBench.run(testClass);

        assertThat(results.getTestName(TestFile.fromClass(testClass), TestId.ROOT), is("OnePassingTest"));
    }

    @Test
    public void the_tests_are_methods_whose_name_starts_with_test() throws InterruptedException {
        Class<OnePassingTest> testClass = OnePassingTest.class;

        SuiteEventDemuxer results = testBench.run(testClass);

        assertThat(results.getTestName(TestFile.fromClass(testClass), TestId.of(0)), is("testPassing"));
    }

    @Test
    public void reports_test_execution() throws InterruptedException {
        SpyListener<RunVisitor> spy = new SpyListener<>(RunVisitor.class);
        RunVisitor listener = spy.getListener();

        Class<OnePassingTest> testClass = OnePassingTest.class;
        TestFile testFile = TestFile.fromClass(testClass);
        listener.onRunStarted(RUN_1, testFile);
        listener.onTestStarted(RUN_1, testFile, TestId.ROOT);
        listener.onTestStarted(RUN_1, testFile, TestId.of(0));
        listener.onTestFinished(RUN_1, testFile, TestId.of(0));
        listener.onTestFinished(RUN_1, testFile, TestId.ROOT);
        listener.onRunFinished(RUN_1, testFile);

        spy.replay();
        SuiteEventDemuxer results = testBench.run(testClass);
        results.visitAllRuns(listener);
        spy.verify();
    }

    @Test
    public void reports_test_failure() throws InterruptedException {
        SpyListener<RunVisitor> spy = new SpyListener<>(RunVisitor.class);
        RunVisitor listener = spy.getListener();

        Class<OneFailingTest> testClass = OneFailingTest.class;
        TestFile testFile = TestFile.fromClass(testClass);
        listener.onRunStarted(RUN_1, testFile);
        listener.onTestStarted(RUN_1, testFile, TestId.ROOT);
        listener.onTestStarted(RUN_1, testFile, TestId.of(0));
        listener.onFailure(RUN_1, testFile, TestId.of(0), StackTrace.copyOf(new AssertionError("dummy failure")));
        listener.onTestFinished(RUN_1, testFile, TestId.of(0));
        listener.onTestFinished(RUN_1, testFile, TestId.ROOT);
        listener.onRunFinished(RUN_1, testFile);

        spy.replay();
        SuiteEventDemuxer results = testBench.run(testClass);
        results.visitAllRuns(listener);
        spy.verify();
    }

    @Test
    public void reports_failures_in_constructor() throws InterruptedException {
        SpyListener<RunVisitor> spy = new SpyListener<>(RunVisitor.class);
        RunVisitor listener = spy.getListener();

        Class<FailureInConstructorTest> testClass = FailureInConstructorTest.class;
        TestFile testFile = TestFile.fromClass(testClass);
        listener.onRunStarted(RUN_1, testFile);
        listener.onTestStarted(RUN_1, testFile, TestId.ROOT);
        listener.onFailure(RUN_1, testFile, TestId.ROOT, StackTrace.copyOf(new RuntimeException("dummy exception")));
        listener.onTestFinished(RUN_1, testFile, TestId.ROOT);
        listener.onRunFinished(RUN_1, testFile);

        spy.replay();
        SuiteEventDemuxer results = testBench.run(testClass);
        results.visitAllRuns(listener);
        spy.verify();

        assertThat("should find the test method even though it fails to run it",
                results.getTestName(TestFile.fromClass(testClass), TestId.of(0)), is("testNotExecuted"));
    }

    @Test
    public void reports_illegal_test_method_signatures() throws InterruptedException {
        SpyListener<RunVisitor> spy = new SpyListener<>(RunVisitor.class);
        RunVisitor listener = spy.getListener();

        Class<IllegalTestMethodSignatureTest> testClass = IllegalTestMethodSignatureTest.class;
        TestFile testFile = TestFile.fromClass(testClass);
        listener.onRunStarted(RUN_1, testFile);
        listener.onTestStarted(RUN_1, testFile, TestId.ROOT);
        listener.onTestStarted(RUN_1, testFile, TestId.of(0));
        listener.onFailure(RUN_1, testFile, TestId.of(0), StackTrace.copyOf(new IllegalArgumentException("wrong number of arguments")));
        listener.onTestFinished(RUN_1, testFile, TestId.of(0));
        listener.onTestFinished(RUN_1, testFile, TestId.ROOT);
        listener.onRunFinished(RUN_1, testFile);

        spy.replay();
        SuiteEventDemuxer results = testBench.run(testClass);
        results.visitAllRuns(listener);
        spy.verify();

        assertThat("should find the test method even though it fails to run it",
                results.getTestName(testFile, TestId.of(0)), is("testMethodWithParameters"));
    }

    @Test
    public void reports_an_error_if_the_test_class_contains_no_test_methods() throws InterruptedException {
        SpyListener<RunVisitor> spy = new SpyListener<>(RunVisitor.class);
        RunVisitor listener = spy.getListener();

        Class<NoTestMethodsTest> testClass = NoTestMethodsTest.class;
        TestFile testFile = TestFile.fromClass(testClass);
        listener.onRunStarted(RUN_1, testFile);
        listener.onTestStarted(RUN_1, testFile, TestId.ROOT);
        listener.onFailure(RUN_1, testFile, TestId.ROOT,
                StackTrace.copyOf(new IllegalArgumentException("No test methods in class fi.jumi.simpleunit.SimpleUnitTest$NoTestMethodsTest")));
        listener.onTestFinished(RUN_1, testFile, TestId.ROOT);
        listener.onRunFinished(RUN_1, testFile);

        spy.replay();
        SuiteEventDemuxer results = testBench.run(testClass);
        results.visitAllRuns(listener);
        spy.verify();
    }

    @Test
    public void if_test_class_annotated_with_RunInDriverThread_then_does_not_use_the_Executor() {
        RunListener runListener = mock(RunListener.class);
        Executor executor = mock(Executor.class);
        Class<AnnotatedWithRunInDriverThreadTest> testClass = AnnotatedWithRunInDriverThreadTest.class;
        SuiteNotifier notifier = new DefaultSuiteNotifier(ActorRef.wrap(runListener), new RunIdSequence(), new OutputCapturer());
        DriverRunner driverRunner = new DriverRunner(new SimpleUnit(), testClass, notifier, executor);

        driverRunner.run();

        verifyZeroInteractions(executor);
        // should anyways run the tests
        verify(runListener).onTestStarted(new RunId(1), TestId.ROOT);
        verify(runListener).onTestStarted(new RunId(1), TestId.of(0));
    }


    // guinea pigs

    @RunVia(SimpleUnit.class)
    @SuppressWarnings({"UnusedDeclaration"})
    public static class OnePassingTest {

        public void testPassing() {
        }

        public void unrelatedMethod() {
            // doesn't start with "test", so is not a test
        }
    }

    @RunVia(SimpleUnit.class)
    @SuppressWarnings({"UnusedDeclaration"})
    public static class OneFailingTest {

        public void testFailing() {
            throw new AssertionError("dummy failure");
        }
    }

    @RunVia(SimpleUnit.class)
    @SuppressWarnings({"UnusedDeclaration"})
    public static class FailureInConstructorTest {

        public FailureInConstructorTest() {
            throw new RuntimeException("dummy exception");
        }

        public void testNotExecuted() {
        }
    }

    @RunVia(SimpleUnit.class)
    @SuppressWarnings({"UnusedDeclaration"})
    public static class IllegalTestMethodSignatureTest {

        public void testMethodWithParameters(Object illegal) {
        }
    }

    @RunVia(SimpleUnit.class)
    @SuppressWarnings({"UnusedDeclaration"})
    public static class NoTestMethodsTest {
    }

    @RunVia(SimpleUnit.class)
    @SuppressWarnings({"UnusedDeclaration"})
    @SimpleUnit.RunInDriverThread
    public static class AnnotatedWithRunInDriverThreadTest {

        public void testUnimportant() {
        }
    }
}
