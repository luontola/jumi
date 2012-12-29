// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import org.junit.*;
import sample.*;

import static org.junit.Assert.fail;

public class RunningTestsTest {

    @Rule
    public final AppRunner app = new AppRunner();


    @Test(timeout = Timeouts.END_TO_END_TEST)
    public void suite_with_zero_tests() throws Exception {
        app.runTestsMatching("glob:sample/NoSuchTest.class");

        app.checkPassingAndFailingTests(0, 0);
        app.checkTotalTestRuns(0);
    }

    @Test(timeout = Timeouts.END_TO_END_TEST)
    public void suite_with_one_passing_test() throws Exception {
        app.runTests(OnePassingTest.class);

        app.checkPassingAndFailingTests(2, 0);
        app.checkTotalTestRuns(1);
        app.checkContainsRun("OnePassingTest", "testPassing", "/", "/");
    }

    @Test(timeout = Timeouts.END_TO_END_TEST)
    public void suite_with_one_failing_test() throws Exception {
        app.runTests(OneFailingTest.class);

        app.checkPassingAndFailingTests(1, 1);
        app.checkTotalTestRuns(1);
        app.checkContainsRun("OneFailingTest", "testFailing", "/", "/");
    }

    @Test(timeout = Timeouts.END_TO_END_TEST)
    public void suite_with_many_test_classes() throws Exception {
        app.runTests(OnePassingTest.class, OneFailingTest.class);

        app.checkPassingAndFailingTests(3, 1);
        app.checkTotalTestRuns(2);
        app.checkContainsRun("OnePassingTest", "testPassing", "/", "/");
        app.checkContainsRun("OneFailingTest", "testFailing", "/", "/");
    }

    @Test(timeout = Timeouts.END_TO_END_TEST)
    public void test_classes_can_be_found_using_file_name_patterns() throws Exception {
        app.runTestsMatching("glob:sample/One{Passing,Failing}Test.class");

        app.checkPassingAndFailingTests(3, 1);
        app.checkTotalTestRuns(2);
        app.checkContainsRun("OnePassingTest", "testPassing", "/", "/");
        app.checkContainsRun("OneFailingTest", "testFailing", "/", "/");
    }

    @Test(timeout = Timeouts.END_TO_END_TEST)
    public void reports_failure_stack_traces() throws Exception {
        app.runTests(OneFailingTest.class);

        app.checkHasStackTrace(
                "java.lang.AssertionError: dummy failure",
                "at sample.OneFailingTest.testFailing");
    }

    @Test(timeout = Timeouts.END_TO_END_TEST)
    public void reports_exceptions_that_are_not_on_the_classpath_of_the_launcher() throws Exception {
        assertNotOnClasspath("sample.extra.CustomException");

        app.runTests(CustomExceptionTest.class);

        app.checkHasStackTrace(
                "sample.extra.CustomException: dummy failure",
                "at sample.CustomExceptionTest.testThrowCustomException");
    }

    private static void assertNotOnClasspath(String className) {
        try {
            Class.forName(className);
            fail("Expected the class to not be on classpath, but it was: " + className);
        } catch (ClassNotFoundException e) {
            // OK
        }
    }

    @Test(timeout = Timeouts.END_TO_END_TEST)
    public void tests_are_run_in_parallel() throws Exception {
        app.runTests(ParallelismTest.class);

        app.checkPassingAndFailingTests(3, 0);
        app.checkTotalTestRuns(2);
        app.checkContainsRun("ParallelismTest", "testOne", "/", "/");
        app.checkContainsRun("ParallelismTest", "testTwo", "/", "/");
    }
}
