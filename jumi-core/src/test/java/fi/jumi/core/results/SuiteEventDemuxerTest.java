// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.results;

import fi.jumi.api.drivers.TestId;
import fi.jumi.core.api.*;
import fi.jumi.core.events.SuiteListenerEventizer;
import fi.jumi.core.util.*;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Method;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class SuiteEventDemuxerTest {

    private static final TestFile TEST_FILE_1 = TestFile.fromClassName("Test1");
    private static final TestFile TEST_FILE_2 = TestFile.fromClassName("Test2");

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private final SuiteEventDemuxer demuxer = new SuiteEventDemuxer();
    private final SuiteListener toDemuxer = new SuiteListenerEventizer().newFrontend(demuxer);

    @Test
    public void allows_runs_to_be_visited_individually() {
        toDemuxer.onRunStarted(new RunId(1), TEST_FILE_1);
        toDemuxer.onRunStarted(new RunId(2), TEST_FILE_2);

        RunVisitor visitor = mock(RunVisitor.class);
        demuxer.visitRun(new RunId(1), visitor);

        verify(visitor).onRunStarted(new RunId(1), TEST_FILE_1);
        verifyNoMoreInteractions(visitor);
    }

    @Test
    public void allows_all_runs_to_be_visited() {
        toDemuxer.onRunStarted(new RunId(1), TEST_FILE_1);
        toDemuxer.onRunStarted(new RunId(2), TEST_FILE_2);

        RunVisitor visitor = mock(RunVisitor.class);
        demuxer.visitAllRuns(visitor);

        verify(visitor).onRunStarted(new RunId(1), TEST_FILE_1);
        verify(visitor).onRunStarted(new RunId(2), TEST_FILE_2);
        verifyNoMoreInteractions(visitor);
    }

    @Test
    public void collects_all_types_of_run_events() {
        RunId runId = new RunId(1);
        TestId testId = TestId.of(1);
        TestFile testFile = TEST_FILE_1;

        toDemuxer.onRunStarted(runId, testFile);
        toDemuxer.onTestStarted(runId, testId);
        toDemuxer.onFailure(runId, StackTrace.copyOf(new Throwable("dummy")));
        toDemuxer.onPrintedOut(runId, "to out");
        toDemuxer.onPrintedErr(runId, "to err");
        toDemuxer.onTestFinished(runId);
        toDemuxer.onRunFinished(runId);

        SpyListener<RunVisitor> spy = new SpyListener<>(RunVisitor.class);
        RunVisitor expect = spy.getListener();

        expect.onRunStarted(runId, testFile);
        expect.onTestStarted(runId, testFile, testId);
        expect.onFailure(runId, testFile, testId, StackTrace.copyOf(new Throwable("dummy")));
        expect.onPrintedOut(runId, testFile, testId, "to out");
        expect.onPrintedErr(runId, testFile, testId, "to err");
        expect.onTestFinished(runId, testFile, testId);
        expect.onRunFinished(runId, testFile);

        checkExpectations(spy, runId);
        checkNoRunEventTypesWereMissedByThisTest(runId);
    }

    private void checkExpectations(SpyListener<RunVisitor> spy, RunId runId) {
        spy.replay();
        demuxer.visitRun(runId, spy.getListener());
        spy.verify();
    }

    private void checkNoRunEventTypesWereMissedByThisTest(RunId runId) {
        MethodCallSpy spy = new MethodCallSpy();
        demuxer.visitRun(runId, spy.createProxyTo(RunVisitor.class));
        SortedSet<String> testedEvents = new TreeSet<>(spy.getMethodCalls());

        assertThat("this test should check all run event types, but did not", testedEvents, is(getMethods(RunVisitor.class)));
    }

    private static SortedSet<String> getMethods(Class<?> type) {
        SortedSet<String> names = new TreeSet<>();
        for (Method method : type.getMethods()) {
            names.add(method.getName());
        }
        return names;
    }

    @Test
    public void supports_printing_after_the_run_is_finished() {
        RunId runId = new RunId(1);
        toDemuxer.onRunStarted(runId, TEST_FILE_1);
        toDemuxer.onTestStarted(runId, TestId.ROOT);
        toDemuxer.onTestFinished(runId);
        toDemuxer.onRunFinished(runId);
        toDemuxer.onPrintedOut(runId, "to out");
        toDemuxer.onPrintedErr(runId, "to err");

        RunVisitor visitor = mock(RunVisitor.class);
        demuxer.visitRun(runId, visitor);

        verify(visitor).onPrintedOut(runId, TEST_FILE_1, null, "to out");
        verify(visitor).onPrintedErr(runId, TEST_FILE_1, null, "to err");
    }

    @Test
    public void tells_when_the_suite_is_finished() {
        assertThat("before finished", demuxer.isSuiteFinished(), is(false));

        toDemuxer.onSuiteFinished();

        assertThat("after finished", demuxer.isSuiteFinished(), is(true));
    }

    @Test
    public void tells_the_names_of_the_tests() {
        // name found
        toDemuxer.onTestFound(TEST_FILE_1, TestId.ROOT, "test name");
        assertThat(demuxer.getTestName(TEST_FILE_1, TestId.ROOT), is("test name"));

        // name not found
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("name not found for Test1 and TestId(0)");
        demuxer.getTestName(TEST_FILE_1, TestId.of(0));
    }

    // TODO: these tests don't fire the onSuiteStarted event; is that a problem? for now the class is quite lenient
}
