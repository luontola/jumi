// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import org.junit.*;
import sample.ContextClassLoaderTest;

public class ClassLoadingTest {

    @Rule
    public final AppRunner app = new AppRunner();

    @Ignore("not implemented") // TODO
    @Test(timeout = Timeouts.END_TO_END_TEST)
    public void context_class_loader_and_current_class_loader_are_the_same() throws Exception {
        app.runTests(ContextClassLoaderTest.class);

        app.checkPassingAndFailingTests(2, 0);
        app.checkTotalTestRuns(1);
    }
}
