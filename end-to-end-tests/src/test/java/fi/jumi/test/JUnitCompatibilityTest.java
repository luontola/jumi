// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import org.junit.*;
import org.junit.rules.Timeout;

import java.io.IOException;

public class JUnitCompatibilityTest {

    @Rule
    public final AppRunner app = new AppRunner();

    @Rule
    public final Timeout timeout = new Timeout(Timeouts.END_TO_END_TEST);


    @Before
    public void addJUnitToPath() throws IOException {
        app.addToClasspath(TestEnvironment.getJUnitJar());
    }

    @Test
    public void runs_JUnit_3_tests() throws Exception {
        app.runTestsMatching("glob:sample/JUnit3Test.class");

        app.checkPassingAndFailingTests(2, 0);
        app.checkTotalTestRuns(1);
        app.checkContainsRun("JUnit3Test", "testSomething", "/", "/");
    }

    @Test
    public void runs_JUnit_4_tests() throws Exception {
        app.runTestsMatching("glob:sample/JUnit4Test.class");

        app.checkPassingAndFailingTests(2, 0);
        app.checkTotalTestRuns(1);
        app.checkContainsRun("JUnit4Test", "something", "/", "/");
    }

    @Test
    public void runs_RunWith_annotated_JUnit_4_tests() throws Exception {
        // We are using org.junit.runners.Suite to test deep test hierarchies at the same time
        app.runTestsMatching("glob:sample/JUnit4AnnotatedTest.class");

        app.checkPassingAndFailingTests(5, 0);
        app.checkTotalTestRuns(2);
        app.checkContainsRun("JUnit4AnnotatedTest", "JUnit4Test", "something", "/", "/", "/");
        app.checkContainsRun("JUnit4AnnotatedTest", "JUnit3Test", "testSomething", "/", "/", "/");
    }

    @Test
    public void reports_test_failures() throws Exception {
        app.runTestsMatching("glob:sample/JUnitFailingTest.class");

        app.checkPassingAndFailingTests(1, 1);
        app.checkTotalTestRuns(1);
        app.checkContainsRun("JUnitFailingTest", "failing", "/", "/");
        app.checkHasStackTrace(
                "java.lang.AssertionError: dummy failure",
                "at sample.JUnitFailingTest.failing");
    }

    @Test
    public void ignored_tests_are_not_run() throws Exception { // TODO: implement support for ignored tests
        app.runTestsMatching("glob:sample/JUnitIgnoredTest.class");

        app.checkEmptyPassingSuite();
    }

    @Test
    public void failing_assumptions_do_not_make_the_test_fail_but_report_the_stack_trace() throws Exception { // TODO: implement support for ignored tests
        app.runTestsMatching("glob:sample/JUnitAssumptionsTest.class");

        app.checkPassingAndFailingTests(2, 0);
        app.checkTotalTestRuns(1);
        app.checkSuitePasses();
        app.checkContainsRun("JUnitAssumptionsTest", "failingAssumption", "/", "/");
        app.checkHasStackTrace(
                "org.junit.internal.AssumptionViolatedException: got: <false>, expected: is <true>",
                "at sample.JUnitAssumptionsTest.failingAssumption");
    }
}
