// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.results;

import fi.jumi.api.drivers.TestId;
import fi.jumi.core.api.*;
import org.junit.Test;

import static fi.jumi.core.results.SuiteProgressMeter.Status.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SuiteProgressMeterTest {

    private final SuiteProgressMeter progressMeter = new SuiteProgressMeter();

    @Test
    public void is_undetermined_until_all_test_files_have_been_found() {
        progressMeter.onSuiteStarted();
        assertThat("after suite started", progressMeter.getStatus(), is(INDETERMINATE));
        assertThat("after suite started", progressMeter.getProgress(), is(0.0));

        progressMeter.onAllTestFilesFound();
        assertThat("after all files found", progressMeter.getStatus(), is(IN_PROGRESS));
    }

    @Test
    public void is_finished_after_the_suite_finishes() {
        progressMeter.onSuiteStarted();
        progressMeter.onAllTestFilesFound();
        assertThat("after all files found", progressMeter.getStatus(), is(IN_PROGRESS));

        progressMeter.onSuiteFinished();
        assertThat("after suite finished", progressMeter.getStatus(), is(COMPLETE));
    }

    @Test
    public void empty_suite() {
        progressMeter.onSuiteStarted();
        progressMeter.onAllTestFilesFound();
        assertThat("after all files found", progressMeter.getProgress(), is(1.0));
    }

    @Test
    public void suite_with_one_test_file() {
        progressMeter.onSuiteStarted();
        progressMeter.onTestFileFound(testFile(1));
        progressMeter.onAllTestFilesFound();
        assertThat("after all files found", progressMeter.getProgress(), is(0.0));

        progressMeter.onTestFileFinished(testFile(1));
        assertThat("after file finished", progressMeter.getProgress(), is(1.0));
    }

    @Test
    public void suite_with_many_test_files() {
        progressMeter.onSuiteStarted();
        progressMeter.onTestFileFound(testFile(1));
        progressMeter.onTestFileFound(testFile(2));
        progressMeter.onAllTestFilesFound();
        assertThat("after all files found", progressMeter.getProgress(), is(0.0));

        progressMeter.onTestFileFinished(testFile(1));
        assertThat("after 1/2 files finished", progressMeter.getProgress(), is(0.5));

        progressMeter.onTestFileFinished(testFile(2));
        assertThat("after 2/2 files finished", progressMeter.getProgress(), is(1.0));
    }

    @Test
    public void test_file_with_many_tests() {
        TestFile file = testFile(1);
        RunId runId = new RunId(1);
        progressMeter.onSuiteStarted();
        progressMeter.onTestFileFound(file);
        progressMeter.onAllTestFilesFound();
        progressMeter.onTestFound(file, TestId.ROOT, "foo");
        progressMeter.onTestFound(file, TestId.of(0), "bar");

        progressMeter.onRunStarted(runId, file);
        progressMeter.onTestStarted(runId, TestId.ROOT);
        assertThat("before any tests finished", progressMeter.getProgress(), is(0.0));

        progressMeter.onTestStarted(runId, TestId.of(0));
        progressMeter.onTestFinished(runId);
        assertThat("after 1/2 tests finished", progressMeter.getProgress(), is(0.5));

        progressMeter.onTestFinished(runId);
        assertThat("after 2/2 tests finished", progressMeter.getProgress(), is(1.0));
    }

    @Test
    public void test_which_is_run_multiple_times() {
        TestFile file = testFile(1);
        progressMeter.onSuiteStarted();
        progressMeter.onTestFileFound(file);
        progressMeter.onAllTestFilesFound();
        progressMeter.onTestFound(file, TestId.ROOT, "foo");

        runTest(file, TestId.ROOT);
        assertThat("after first run", progressMeter.getProgress(), is(1.0));

        runTest(file, TestId.ROOT);
        assertThat("after repeat run", progressMeter.getProgress(), is(1.0));
    }

    @Test
    public void multiple_tests_are_found_but_only_some_are_run() {
        TestFile file = testFile(1);
        progressMeter.onSuiteStarted();
        progressMeter.onTestFileFound(file);
        progressMeter.onAllTestFilesFound();
        progressMeter.onTestFound(file, TestId.ROOT, "foo");
        progressMeter.onTestFound(file, TestId.of(0), "bar");

        runTest(file, TestId.ROOT);
        assertThat("after 1/2 tests finished", progressMeter.getProgress(), is(0.5));

        progressMeter.onTestFileFinished(file);
        assertThat("after test file finished (but only 1/2 tests finished)", progressMeter.getProgress(), is(1.0));
    }

    @Test
    public void multiple_test_files_with_multiple_tests() {
        TestFile file1 = testFile(1);
        TestFile file2 = testFile(2);
        progressMeter.onSuiteStarted();
        progressMeter.onTestFileFound(file1);
        progressMeter.onTestFileFound(file2);
        progressMeter.onAllTestFilesFound();
        progressMeter.onTestFound(file1, TestId.ROOT, "test 1.1");
        progressMeter.onTestFound(file1, TestId.of(0), "test 1.2");
        progressMeter.onTestFound(file2, TestId.ROOT, "test 2.1");
        progressMeter.onTestFound(file2, TestId.of(0), "test 2.2");
        progressMeter.onTestFound(file2, TestId.of(1), "test 2.3");
        assertThat("before any tests finished", progressMeter.getProgress(), is(0.0));

        runTest(file1, TestId.ROOT);
        assertThat("after 1/2 + 0/3 tests finished", progressMeter.getProgress(), is(0.25));

        runTest(file2, TestId.ROOT);
        assertThat("after 1/2 + 1/3 tests finished", progressMeter.getProgress(), is(closeTo(0.416, 0.001)));

        runTest(file2, TestId.of(0));
        assertThat("after 1/2 + 2/3 tests finished", progressMeter.getProgress(), is(closeTo(0.583, 0.001)));

        runTest(file2, TestId.of(1));
        assertThat("after 1/2 + 3/3 tests finished", progressMeter.getProgress(), is(0.75));

        runTest(file1, TestId.of(0));
        assertThat("after 2/2 + 3/3 tests finished", progressMeter.getProgress(), is(1.0));
    }


    // helpers

    private int nextRunId = 1;

    private void runTest(TestFile testFile, TestId testId) {
        RunId runId = uniqueRunId();
        progressMeter.onRunStarted(runId, testFile);
        progressMeter.onTestStarted(runId, testId);
        progressMeter.onTestFinished(runId);
        progressMeter.onRunFinished(runId);
    }

    private RunId uniqueRunId() {
        return new RunId(nextRunId++);
    }

    private static TestFile testFile(int id) {
        return TestFile.fromClassName("TestFile" + id);
    }
}
