// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import org.junit.*;
import sample.BuggyDriverTest;

import static fi.jumi.core.util.AsyncAssert.assertEventually;
import static fi.jumi.core.util.StringMatchers.containsSubStrings;
import static org.hamcrest.Matchers.hasProperty;

public class ErrorHandlingTest {

    @Rule
    public final AppRunner app = new AppRunner();

    @Test(timeout = Timeouts.END_TO_END_TEST)
    public void reports_uncaught_exceptions_from_actor_threads() throws Exception {
        app.runTestsMatching("glob:sample/extra/CorruptTest.class");

        assertReportsInternalError(
                "Uncaught exception in thread jumi-actor-",
                "java.lang.ClassFormatError");
    }

    @Test(timeout = Timeouts.END_TO_END_TEST)
    public void reports_uncaught_exceptions_from_driver_threads() throws Exception {
        app.runTests(BuggyDriverTest.class);

        assertReportsInternalError(
                "Uncaught exception in thread jumi-test-",
                "java.lang.RuntimeException: dummy exception from driver thread");
    }

    @Test(timeout = Timeouts.END_TO_END_TEST)
    public void reports_uncaught_exceptions_from_test_threads() throws Exception {
        app.runTests(BuggyDriverTest.class);

        assertReportsInternalError(
                "Uncaught exception in thread jumi-test-",
                "java.lang.RuntimeException: dummy exception from test thread");
    }

    private void assertReportsInternalError(String... expectedErrorMessages) {
        app.checkHasStackTrace(expectedErrorMessages);
        app.checkPassingAndFailingTests(0, 0);
        app.checkTotalTestRuns(0);
        assertEventually("internal errors should have been logged",
                app, hasProperty("currentDaemonOutput", containsSubStrings(expectedErrorMessages)), Timeouts.ASSERTION);
    }
}
