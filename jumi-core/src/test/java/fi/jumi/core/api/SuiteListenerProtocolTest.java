// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.api;

import fi.jumi.api.drivers.*;
import fi.jumi.core.drivers.DriverFinder;
import fi.jumi.core.suite.SuiteRunnerIntegrationHelper;
import org.junit.Test;

import java.util.concurrent.Executor;

public class SuiteListenerProtocolTest extends SuiteRunnerIntegrationHelper {

    private static final RunId RUN_1 = new RunId(1);
    private static final RunId RUN_2 = new RunId(2);
    private static final Class<?> TEST_CLASS_1 = DummyTest.class;
    private static final TestFile TEST_FILE_1 = TestFile.fromClass(TEST_CLASS_1);
    private static final Class<?> TEST_CLASS_2 = SecondDummyTest.class;
    private static final TestFile TEST_FILE_2 = TestFile.fromClass(TEST_CLASS_2);

    @Test
    public void suite_with_zero_test_classes() {
        expect.onSuiteStarted();
        expect.onAllTestFilesFound();
        expect.onSuiteFinished();

        runAndCheckExpectations(DriverFinder.DRIVER_NOT_FOUND);
    }

    @Test
    public void suite_with_one_test_class_with_zero_tests() {
        expect.onSuiteStarted();
        expect.onTestFileFound(TEST_FILE_1);
        expect.onAllTestFilesFound();
        expect.onTestFound(TEST_FILE_1, TestId.ROOT, "DummyTest");
        expect.onSuiteFinished();

        runAndCheckExpectations(new ZeroTestsDriver(), TEST_CLASS_1);
    }

    @Test
    public void suite_with_one_test_class_with_tests() {
        expect.onSuiteStarted();
        expect.onTestFileFound(TEST_FILE_1);
        expect.onAllTestFilesFound();
        expect.onTestFound(TEST_FILE_1, TestId.ROOT, "DummyTest");
        expect.onRunStarted(RUN_1, TEST_FILE_1);
        expect.onTestStarted(RUN_1, TestId.ROOT);
        expect.onTestFinished(RUN_1);
        expect.onRunFinished(RUN_1);
        expect.onSuiteFinished();

        runAndCheckExpectations(new OneTestDriver(), TEST_CLASS_1);
    }

    @Test
    public void suite_with_printing_tests() {
        expect.onSuiteStarted();
        expect.onTestFileFound(TEST_FILE_1);
        expect.onAllTestFilesFound();
        expect.onTestFound(TEST_FILE_1, TestId.ROOT, "DummyTest");
        expect.onRunStarted(RUN_1, TEST_FILE_1);
        expect.onTestStarted(RUN_1, TestId.ROOT);
        expect.onPrintedOut(RUN_1, "printed to stdout");
        expect.onPrintedErr(RUN_1, "printed to stderr");
        expect.onTestFinished(RUN_1);
        expect.onRunFinished(RUN_1);
        expect.onSuiteFinished();

        runAndCheckExpectations(new OnePrintingTestDriver(), TEST_CLASS_1);
    }

    @Test
    public void suite_with_failing_tests() {
        expect.onSuiteStarted();
        expect.onTestFileFound(TEST_FILE_1);
        expect.onAllTestFilesFound();
        expect.onTestFound(TEST_FILE_1, TestId.ROOT, "DummyTest");
        expect.onRunStarted(RUN_1, TEST_FILE_1);
        expect.onTestStarted(RUN_1, TestId.ROOT);
        expect.onFailure(RUN_1, StackTrace.copyOf(new Exception("dummy failure")));
        expect.onTestFinished(RUN_1);
        expect.onRunFinished(RUN_1);
        expect.onSuiteFinished();

        runAndCheckExpectations(new OneFailingTestDriver(), TEST_CLASS_1);
    }

    @Test
    public void suite_with_nested_tests() {
        expect.onSuiteStarted();
        expect.onTestFileFound(TEST_FILE_1);
        expect.onAllTestFilesFound();
        expect.onTestFound(TEST_FILE_1, TestId.ROOT, "DummyTest");

        expect.onTestFound(TEST_FILE_1, TestId.of(0), "parent test");
        expect.onRunStarted(RUN_1, TEST_FILE_1);
        expect.onTestStarted(RUN_1, TestId.of(0));
        {
            expect.onTestFound(TEST_FILE_1, TestId.of(0, 0), "child test");
            expect.onTestStarted(RUN_1, TestId.of(0, 0));
            expect.onTestFinished(RUN_1);
        }
        expect.onTestFinished(RUN_1);
        expect.onRunFinished(RUN_1);

        expect.onSuiteFinished();

        runAndCheckExpectations(new NestedTestsDriver(), TEST_CLASS_1);
    }

    @Test
    public void suite_with_many_runs() {
        expect.onSuiteStarted();
        expect.onTestFileFound(TEST_FILE_1);
        expect.onAllTestFilesFound();
        expect.onTestFound(TEST_FILE_1, TestId.ROOT, "DummyTest");

        expect.onTestFound(TEST_FILE_1, TestId.of(0), "test one");
        expect.onRunStarted(RUN_1, TEST_FILE_1);
        expect.onTestStarted(RUN_1, TestId.of(0));
        expect.onTestFinished(RUN_1);
        expect.onRunFinished(RUN_1);

        expect.onTestFound(TEST_FILE_1, TestId.of(1), "test two");
        expect.onRunStarted(RUN_2, TEST_FILE_1);
        expect.onTestStarted(RUN_2, TestId.of(1));
        expect.onTestFinished(RUN_2);
        expect.onRunFinished(RUN_2);

        expect.onSuiteFinished();

        runAndCheckExpectations(new ManyTestRunsDriver(), TEST_CLASS_1);
    }

    @Test
    public void suite_with_many_test_classes() {
        expect.onSuiteStarted();
        expect.onTestFileFound(TEST_FILE_1);
        expect.onTestFileFound(TEST_FILE_2);
        expect.onAllTestFilesFound();

        expect.onTestFound(TEST_FILE_1, TestId.ROOT, "DummyTest");
        expect.onTestFound(TEST_FILE_2, TestId.ROOT, "SecondDummyTest");

        expect.onRunStarted(new RunId(1), TEST_FILE_1);
        expect.onTestStarted(new RunId(1), TestId.ROOT);
        expect.onTestFinished(new RunId(1));
        expect.onRunFinished(new RunId(1));

        expect.onRunStarted(new RunId(2), TEST_FILE_2);
        expect.onTestStarted(new RunId(2), TestId.ROOT);
        expect.onTestFinished(new RunId(2));
        expect.onRunFinished(new RunId(2));

        expect.onSuiteFinished();

        runAndCheckExpectations(new OneTestDriver(), TEST_CLASS_1, TEST_CLASS_2);
    }


    // guinea pigs

    private static class DummyTest {
    }

    private static class SecondDummyTest {
    }

    public static class ZeroTestsDriver extends Driver {
        @Override
        public void findTests(Class<?> testClass, SuiteNotifier notifier, Executor executor) {
            notifier.fireTestFound(TestId.ROOT, testClass.getSimpleName());
        }
    }

    public static class OneTestDriver extends Driver {
        @Override
        public void findTests(Class<?> testClass, final SuiteNotifier notifier, Executor executor) {
            notifier.fireTestFound(TestId.ROOT, testClass.getSimpleName());
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    TestNotifier tn = notifier.fireTestStarted(TestId.ROOT);
                    tn.fireTestFinished();
                }
            });
        }
    }

    public class OnePrintingTestDriver extends Driver {
        @Override
        public void findTests(Class<?> testClass, final SuiteNotifier notifier, Executor executor) {
            notifier.fireTestFound(TestId.ROOT, testClass.getSimpleName());
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    TestNotifier tn = notifier.fireTestStarted(TestId.ROOT);
                    stdout.print("printed to stdout");
                    stderr.print("printed to stderr");
                    tn.fireTestFinished();
                }
            });
        }
    }

    public static class OneFailingTestDriver extends Driver {
        @Override
        public void findTests(Class<?> testClass, final SuiteNotifier notifier, Executor executor) {
            notifier.fireTestFound(TestId.ROOT, testClass.getSimpleName());
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    TestNotifier tn = notifier.fireTestStarted(TestId.ROOT);
                    tn.fireFailure(new Exception("dummy failure"));
                    tn.fireTestFinished();
                }
            });
        }
    }

    public static class NestedTestsDriver extends Driver {
        @Override
        public void findTests(final Class<?> testClass, final SuiteNotifier notifier, Executor executor) {
            notifier.fireTestFound(TestId.ROOT, testClass.getSimpleName());
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    notifier.fireTestFound(TestId.of(0), "parent test");
                    TestNotifier parent = notifier.fireTestStarted(TestId.of(0));

                    notifier.fireTestFound(TestId.of(0, 0), "child test");
                    TestNotifier child = notifier.fireTestStarted(TestId.of(0, 0));

                    child.fireTestFinished();
                    parent.fireTestFinished();
                }
            });
        }
    }

    public static class ManyTestRunsDriver extends Driver {
        @Override
        public void findTests(final Class<?> testClass, final SuiteNotifier notifier, Executor executor) {
            notifier.fireTestFound(TestId.ROOT, testClass.getSimpleName());
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    notifier.fireTestFound(TestId.of(0), "test one");
                    notifier.fireTestStarted(TestId.of(0))
                            .fireTestFinished();
                }
            });
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    notifier.fireTestFound(TestId.of(1), "test two");
                    notifier.fireTestStarted(TestId.of(1))
                            .fireTestFinished();
                }
            });
        }
    }
}
