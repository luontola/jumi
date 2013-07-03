// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import org.junit.*;
import org.junit.rules.Timeout;
import sample.*;

public class IgnoringTestsTest {

    @Rule
    public final AppRunner app = new AppRunner();

    @Rule
    public final Timeout timeout = new Timeout(Timeouts.END_TO_END_TEST);


    @Test
    public void silently_ignores_abstract_test_classes() throws Exception {
        app.runTests(AbstractTest.class);

        app.checkEmptyPassingSuite();
    }

    @Test
    public void silently_ignores_non_test_classes_that_anyways_match_the_file_name_pattern() throws Exception {
        app.runTests(NotReallyTest.class);

        app.checkEmptyPassingSuite();
    }
}
