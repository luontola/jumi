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

    @Test(timeout = Timeouts.END_TO_END_TEST)
    public void runs_JUnit_3_tests() throws Exception {
        app.runTestsMatching("glob:sample/JUnit3Test.class");

        app.checkPassingAndFailingTests(2, 0);
        app.checkTotalTestRuns(1);
        app.checkContainsRun("JUnit3Test", "testSomething", "/", "/");
    }

    @Test(timeout = Timeouts.END_TO_END_TEST)
    public void runs_JUnit_4_tests() throws Exception {
        app.runTestsMatching("glob:sample/JUnit4Test.class");

        app.checkPassingAndFailingTests(2, 0);
        app.checkTotalTestRuns(1);
        app.checkContainsRun("JUnit4Test", "something", "/", "/");
    }

    @Ignore("not implemented")
    @Test(timeout = Timeouts.END_TO_END_TEST)
    public void runs_RunWith_annotated_JUnit_4_tests() throws Exception {
        app.runTestsMatching("glob:sample/JUnit4AnnotatedTest.class");

        app.checkPassingAndFailingTests(5, 0);
        app.checkTotalTestRuns(1);
        app.checkContainsRun("JUnit4AnnotatedTest",
                "JUnit4Test", "something", "/", "/",
                "JUnit3Test", "testSomething", "/", "/",
                "/");
    }

    // TODO: check in unit tests that multiple tests, failures etc. are handled correctly
}
