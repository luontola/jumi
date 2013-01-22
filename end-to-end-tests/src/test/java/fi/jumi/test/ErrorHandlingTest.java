// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import org.junit.*;

public class ErrorHandlingTest {

    @Rule
    public final AppRunner app = new AppRunner();

    @Ignore("not implemented") // TODO
    @Test(timeout = Timeouts.END_TO_END_TEST)
    public void reports_uncaught_exceptions_from_actor_threads() throws Exception {
        app.runTestsMatching("glob:sample/extra/CorruptTest.class");

        app.checkHasStackTrace(
                "Uncaught exception in thread jumi-actors-",
                "java.lang.ClassFormatError");
        app.checkPassingAndFailingTests(0, 0);
        app.checkTotalTestRuns(0);
    }
}
