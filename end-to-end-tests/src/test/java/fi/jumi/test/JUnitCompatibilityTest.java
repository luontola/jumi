// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import org.junit.*;

import java.io.IOException;

public class JUnitCompatibilityTest {

    @Rule
    public final AppRunner app = new AppRunner();

    @Before
    public void addJUnitToPath() throws IOException {
        app.addToClasspath(TestEnvironment.getProjectJar("junit"));
    }

    @Ignore("not implemented")
    @Test(timeout = Timeouts.END_TO_END_TEST)
    public void runs_JUnit_3_tests() throws Exception {
        app.runTestsMatching("glob:sample/JUnit3Test.class");

        app.checkPassingAndFailingTests(2, 0);
        app.checkTotalTestRuns(1);
        app.checkContainsRun("JUnit3Test", "testSomething", "/", "/");
    }

    // TODO: runs JUnit 4 tests
    // TODO: runs JUnit 4 custom runners annotated with RunWith
}
