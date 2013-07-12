// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.results;

import fi.jumi.api.drivers.TestId;
import fi.jumi.core.api.*;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SuiteResultsSummaryTest {

    private static final TestFile TEST_FILE = TestFile.fromClassName("TestFile");

    private final SuiteResultsSummary summary = new SuiteResultsSummary();
    private final RunId unimportant = null;

    @Test
    public void passing_tests_are_tests_without_failures() {
        summary.onTestStarted(unimportant, TEST_FILE, TestId.ROOT);

        assertThat("passing tests", summary.getPassingTests(), is(1));
        assertThat("failing tests", summary.getFailingTests(), is(0));
        assertThat("total tests", summary.getTotalTests(), is(1));
    }

    @Test
    public void failing_tests_are_tests_with_failures() {
        summary.onTestStarted(unimportant, TEST_FILE, TestId.ROOT);
        summary.onFailure(unimportant, TEST_FILE, TestId.ROOT, StackTrace.copyOf(new Throwable("failure")));

        assertThat("passing tests", summary.getPassingTests(), is(0));
        assertThat("failing tests", summary.getFailingTests(), is(1));
        assertThat("total tests", summary.getTotalTests(), is(1));
    }

    @Test
    public void a_test_with_multiple_failures_counts_as_just_one_failing_test() {
        summary.onTestStarted(unimportant, TEST_FILE, TestId.ROOT);
        summary.onFailure(unimportant, TEST_FILE, TestId.ROOT, StackTrace.copyOf(new Throwable("failure 1")));
        summary.onFailure(unimportant, TEST_FILE, TestId.ROOT, StackTrace.copyOf(new Throwable("failure 2")));

        assertThat("passing tests", summary.getPassingTests(), is(0));
        assertThat("failing tests", summary.getFailingTests(), is(1));
        assertThat("total tests", summary.getTotalTests(), is(1));
    }

    @Test
    public void multiple_executions_of_the_same_test_counts_as_just_one_test() {
        summary.onTestStarted(new RunId(1), TEST_FILE, TestId.ROOT);
        summary.onTestStarted(new RunId(2), TEST_FILE, TestId.ROOT);

        assertThat("total tests", summary.getTotalTests(), is(1));
    }

    @Test
    public void tests_are_uniquely_identified_by_their_TestFile_and_TestId() {
        summary.onTestStarted(unimportant, TEST_FILE, TestId.ROOT);
        summary.onTestStarted(unimportant, TEST_FILE, TestId.of(1)); // different TestId
        summary.onTestStarted(unimportant, TestFile.fromClassName("TestFile2"), TestId.ROOT); // different TestFile

        assertThat("total tests", summary.getTotalTests(), is(3));
    }
}
