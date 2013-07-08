// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runs;


import fi.jumi.actors.ActorRef;
import fi.jumi.api.drivers.*;
import fi.jumi.core.output.OutputCapturer;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.mockito.InOrder;

import java.io.PrintStream;
import java.util.concurrent.*;

import static org.fest.assertions.Fail.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
public class ThreadBoundSuiteNotifierTest {

    private static final RunId FIRST_RUN_ID = new RunId(RunId.FIRST_ID);

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private final RunListener listener = mock(RunListener.class);
    private final OutputCapturer outputCapturer = new OutputCapturer();
    private final PrintStream stdout = outputCapturer.out();
    private Throwable lastError;

    private final SuiteNotifier notifier = new ThreadBoundSuiteNotifier(ActorRef.wrap(listener), new RunIdSequence(), outputCapturer);

    @Test
    public void notifies_about_the_beginning_and_end_of_a_run() {
        TestNotifier tn1 = notifier.fireTestStarted(TestId.ROOT);
        TestNotifier tn2 = notifier.fireTestStarted(TestId.of(0));
        tn2.fireTestFinished();
        tn1.fireTestFinished();

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).onRunStarted(FIRST_RUN_ID);
        inOrder.verify(listener).onTestStarted(FIRST_RUN_ID, TestId.ROOT);
        inOrder.verify(listener).onTestStarted(FIRST_RUN_ID, TestId.of(0));
        inOrder.verify(listener).onTestFinished(FIRST_RUN_ID, TestId.of(0));
        inOrder.verify(listener).onTestFinished(FIRST_RUN_ID, TestId.ROOT);
        inOrder.verify(listener).onRunFinished(FIRST_RUN_ID);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void captures_what_is_printed_during_a_run() {
        TestNotifier tn1 = notifier.fireTestStarted(TestId.ROOT);
        stdout.print("1");
        TestNotifier tn2 = notifier.fireTestStarted(TestId.of(0));
        stdout.print("2");
        tn2.fireTestFinished();
        stdout.print("3");
        tn1.fireTestFinished();

        verify(listener).onPrintedOut(FIRST_RUN_ID, "1");
        verify(listener).onPrintedOut(FIRST_RUN_ID, "2");
        verify(listener).onPrintedOut(FIRST_RUN_ID, "3");
    }

    @Test
    public void does_not_capture_what_is_printed_outside_a_run() {
        stdout.print("before");
        TestNotifier tn1 = notifier.fireTestStarted(TestId.ROOT);
        tn1.fireTestFinished();
        stdout.print("after");

        verify(listener, never()).onPrintedOut(any(RunId.class), anyString());
    }

    @Test
    public void forwards_internal_errors_to_the_listener() {
        Throwable cause = new Throwable("dummy exception");
        notifier.fireInternalError("the message", cause);

        verify(listener).onInternalError("the message", cause);
    }


    // bulletproofing the public API

    @Test
    public void fireFailure_must_be_called_on_the_current_test() {
        final TestNotifier tn1 = notifier.fireTestStarted(TestId.ROOT);
        TestNotifier tn2 = notifier.fireTestStarted(TestId.of(0));

        expectIllegalStateException("must be called on the innermost non-finished TestNotifier; " +
                "expected TestNotifier(RunId(1), [TestId(), TestId(0)]) " +
                "but was TestNotifier(RunId(1), [TestId()]) which is not innermost", new Runnable() {
            @Override
            public void run() {
                tn1.fireFailure(new Exception("dummy"));
            }
        });

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).onRunStarted(FIRST_RUN_ID);
        inOrder.verify(listener).onTestStarted(FIRST_RUN_ID, TestId.ROOT);
        inOrder.verify(listener).onTestStarted(FIRST_RUN_ID, TestId.of(0));
        inOrder.verify(listener).onInternalError("Incorrect notifier API usage", lastError);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void fireFailure_cannot_be_called_after_the_test_is_finished() {
        TestNotifier tn1 = notifier.fireTestStarted(TestId.ROOT);
        final TestNotifier tn2 = notifier.fireTestStarted(TestId.of(0));
        tn2.fireTestFinished();

        expectIllegalStateException("must be called on the innermost non-finished TestNotifier; " +
                "expected TestNotifier(RunId(1), [TestId()]) " +
                "but was TestNotifier(RunId(1), [TestId(), TestId(0)]) which is finished", new Runnable() {
            @Override
            public void run() {
                tn2.fireFailure(new Exception("dummy"));
            }
        });

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).onRunStarted(FIRST_RUN_ID);
        inOrder.verify(listener).onTestStarted(FIRST_RUN_ID, TestId.ROOT);
        inOrder.verify(listener).onTestStarted(FIRST_RUN_ID, TestId.of(0));
        inOrder.verify(listener).onTestFinished(FIRST_RUN_ID, TestId.of(0));
        inOrder.verify(listener).onInternalError("Incorrect notifier API usage", lastError);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void error_in_fireFailure_will_not_lose_the_original_test_failure_being_reported() {
        final TestNotifier tn1 = notifier.fireTestStarted(TestId.ROOT);
        TestNotifier tn2 = notifier.fireTestStarted(TestId.of(0));

        Throwable originalTestFailure = new Exception("original test failure");
        try {
            tn1.fireFailure(originalTestFailure);
            fail("should have thrown an exception");
        } catch (IllegalStateException e) {
            assertThat("chained exception", e.getCause(), is(originalTestFailure));
        }
    }

    @Test
    public void fireTestFinished_must_be_called_on_the_current_test() {
        final TestNotifier tn1 = notifier.fireTestStarted(TestId.ROOT);
        TestNotifier tn2 = notifier.fireTestStarted(TestId.of(0));

        expectIllegalStateException("must be called on the innermost non-finished TestNotifier; " +
                "expected TestNotifier(RunId(1), [TestId(), TestId(0)]) " +
                "but was TestNotifier(RunId(1), [TestId()]) which is not innermost", new Runnable() {
            @Override
            public void run() {
                tn1.fireTestFinished();
            }
        });

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).onRunStarted(FIRST_RUN_ID);
        inOrder.verify(listener).onTestStarted(FIRST_RUN_ID, TestId.ROOT);
        inOrder.verify(listener).onTestStarted(FIRST_RUN_ID, TestId.of(0));
        inOrder.verify(listener).onInternalError("Incorrect notifier API usage", lastError);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void fireTestFinished_cannot_be_called_after_the_test_is_finished() {
        TestNotifier tn1 = notifier.fireTestStarted(TestId.ROOT);
        final TestNotifier tn2 = notifier.fireTestStarted(TestId.of(0));
        tn2.fireTestFinished();

        expectIllegalStateException("must be called on the innermost non-finished TestNotifier; " +
                "expected TestNotifier(RunId(1), [TestId()]) " +
                "but was TestNotifier(RunId(1), [TestId(), TestId(0)]) which is finished", new Runnable() {
            @Override
            public void run() {
                tn2.fireTestFinished();
            }
        });

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).onRunStarted(FIRST_RUN_ID);
        inOrder.verify(listener).onTestStarted(FIRST_RUN_ID, TestId.ROOT);
        inOrder.verify(listener).onTestStarted(FIRST_RUN_ID, TestId.of(0));
        inOrder.verify(listener).onTestFinished(FIRST_RUN_ID, TestId.of(0));
        inOrder.verify(listener).onInternalError("Incorrect notifier API usage", lastError);
        verifyNoMoreInteractions(listener);
    }

    /**
     * Reproduces an issue with the specs2 testing framework's JUnit integration, which starts each test in their own
     * threads but sends all test finished events from a common thread.
     */
    @Test
    public void fireTestFinished_may_be_called_from_a_different_thread_than_in_which_the_test_run_was_started() throws Exception {
        TestNotifier tn = inNewThread(new Callable<TestNotifier>() {
            @Override
            public TestNotifier call() throws Exception {
                return notifier.fireTestStarted(TestId.ROOT);
            }
        });
        tn.fireTestFinished();

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).onRunStarted(FIRST_RUN_ID);
        inOrder.verify(listener).onTestStarted(FIRST_RUN_ID, TestId.ROOT);
        inOrder.verify(listener).onTestFinished(FIRST_RUN_ID, TestId.ROOT);
        inOrder.verify(listener).onRunFinished(FIRST_RUN_ID);
        verifyNoMoreInteractions(listener);
    }

    /**
     * Although unlikely, it's allowed for a TestId invocation to nest invocations of the same TestId. Thus any error
     * checking must use the TestNotifier instance and not just check the TestId.
     */
    @Test
    public void error_checking_is_based_on_TestNotifier_instances_instead_of_TestId_values() {
        final TestNotifier tn1 = notifier.fireTestStarted(TestId.ROOT);
        TestNotifier tn2 = notifier.fireTestStarted(TestId.ROOT);

        expectIllegalStateException("must be called on the innermost non-finished TestNotifier; " +
                "expected TestNotifier(RunId(1), [TestId(), TestId()]) " +
                "but was TestNotifier(RunId(1), [TestId()]) which is not innermost", new Runnable() {
            @Override
            public void run() {
                tn1.fireTestFinished();
            }
        });

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).onRunStarted(FIRST_RUN_ID);
        inOrder.verify(listener, times(2)).onTestStarted(FIRST_RUN_ID, TestId.ROOT);
        inOrder.verify(listener).onInternalError("Incorrect notifier API usage", lastError);
        verifyNoMoreInteractions(listener);
    }


    // helpers

    private void expectIllegalStateException(String expectedMessage, Runnable command) {
        try {
            command.run();
            fail("should have thrown an IllegalStateException");
        } catch (IllegalStateException e) {
            lastError = e;
            assertThat("assertion message", e.getMessage(), is(expectedMessage));
        }
    }

    private static <T> T inNewThread(Callable<T> callable) throws Exception {
        ExecutorService executor = Executors.newCachedThreadPool();
        try {
            return executor.submit(callable).get();
        } finally {
            executor.shutdownNow();
        }
    }
}
