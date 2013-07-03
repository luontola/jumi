// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import org.junit.*;
import org.junit.rules.Timeout;
import sample.*;

import static fi.jumi.core.util.AsyncAssert.assertEventually;
import static fi.jumi.core.util.StringMatchers.containsSubStrings;
import static org.hamcrest.Matchers.hasProperty;

public class InternalErrorReportingTest {

    @Rule
    public final AppRunner app = new AppRunner();

    @Rule
    public final Timeout timeout = new Timeout(Timeouts.END_TO_END_TEST);


    @Test
    public void reports_uncaught_exceptions_from_actor_threads() throws Exception {
        app.runTestsMatching("glob:sample/extra/CorruptTest.class");

        assertReportsInternalError(
                "Uncaught exception in thread jumi-actor-",
                "java.lang.ClassFormatError");
    }

    @Test
    public void reports_uncaught_exceptions_from_driver_threads() throws Exception {
        app.runTests(BuggyDriverTest.class);

        assertReportsInternalError(
                "Uncaught exception in thread jumi-test-",
                "java.lang.RuntimeException: dummy exception from driver thread");
    }

    @Test
    public void reports_uncaught_exceptions_from_test_threads() throws Exception {
        app.runTests(BuggyDriverTest.class);

        assertReportsInternalError(
                "Uncaught exception in thread jumi-test-",
                "java.lang.RuntimeException: dummy exception from test thread");
    }

    @Test
    public void reports_it_if_starting_the_daemon_process_failed() throws Exception {
        app.daemon.setStartupTimeout(500); // TODO: detect it if the daemon process dies before the timeout? (to avoid this long timeout slowing down this test)

        app.suite.addJvmOptions("-Xmx1M"); // too small heap space for the JVM to start
        app.runTests(OnePassingTest.class);

        app.checkHasStackTrace(
                "Failed to start the test runner daemon process",
                "timed out after 500 ms"); // TODO: show the path of where the JVM output was logged
    }

    @Test
    public void reports_it_if_the_daemon_process_dies_in_the_middle_of_a_suite() throws Exception {
        app.runTests(DaemonKillingTest.class);
        // TODO: if this test becomes flaky: wait for the onSuiteStarted event, then signal DaemonKillingTest (by creating a temp file) to kill the process

        app.checkHasStackTrace("The test runner daemon process disconnected or died unexpectedly"); // TODO: show the path of where the JVM output was logged
    }


    private void assertReportsInternalError(String... expectedErrorMessages) {
        app.checkHasStackTrace(expectedErrorMessages);
        app.checkPassingAndFailingTests(0, 0);
        app.checkTotalTestRuns(0);
        assertEventually("internal errors should have been logged",
                app, hasProperty("currentDaemonOutput", containsSubStrings(expectedErrorMessages)), Timeouts.ASSERTION);
    }
}
