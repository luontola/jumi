// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.junit;

import fi.jumi.api.drivers.TestId;
import fi.jumi.core.api.TestFile;
import fi.jumi.core.drivers.JUnitCompatibilityDriverFinder;
import fi.jumi.core.results.SuiteEventDemuxer;
import fi.jumi.core.testbench.TestBench;
import junit.framework.TestCase;
import org.junit.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class JUnitCompatibilityDriverTest {

    private final TestBench testBench = new TestBench();

    @Before
    public void setup() throws Exception {
        testBench.setDriverFinder(new JUnitCompatibilityDriverFinder(getClass().getClassLoader()));
//        testBench.setActorsMessageListener(new PrintStreamMessageLogger(System.out));
    }

    @Test
    public void runs_JUnit_3_tests() {
        Class<?> testClass = OnePassingJUnit3.class;
        TestFile testFile = TestFile.fromClass(testClass);
        SuiteEventDemuxer results = testBench.run(testClass);

        assertThat(results.getTestName(testFile, TestId.ROOT), is(testClass.getSimpleName()));
        assertThat(results.getTestName(testFile, TestId.of(0)), is("testPassing"));
    }

    // TODO: JUnit 4
    // TODO: multiple test methods
    // TODO: deep test hierarchies
    // TODO: failures
    // TODO: ignored tests
    // TODO: assumptions

    public static class OnePassingJUnit3 extends TestCase {
        public void testPassing() {
        }
    }
}
