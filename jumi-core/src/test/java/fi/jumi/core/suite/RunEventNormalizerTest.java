// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.suite;

import fi.jumi.api.drivers.TestId;
import fi.jumi.core.api.*;
import fi.jumi.core.runs.RunId;
import org.junit.*;
import org.junit.rules.ExpectedException;

import static org.mockito.Mockito.*;

public class RunEventNormalizerTest {

    private final SuiteListener target = mock(SuiteListener.class);
    private final TestFile testFile = TestFile.fromClassName("com.example.SomeTest");
    private final RunEventNormalizer normalizer = new RunEventNormalizer(target, testFile);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void forwards_unique_onTestFound_events() {
        normalizer.onTestFound(TestId.ROOT, "root");
        normalizer.onTestFound(TestId.of(1), "testOne");

        verify(target).onTestFound(testFile, TestId.ROOT, "root");
        verify(target).onTestFound(testFile, TestId.of(1), "testOne");
        verifyNoMoreInteractions(target);
    }

    @Test
    public void forwards_all_other_events() {
        // TODO: create a generic test which calls all methods except onTestFound
        normalizer.onPrintedOut(new RunId(7), "stdout");
        normalizer.onPrintedErr(new RunId(8), "stderr");
        normalizer.onFailure(new RunId(9), TestId.of(1), new Exception("dummy exception"));
        normalizer.onTestStarted(new RunId(10), TestId.of(2));
        normalizer.onTestFinished(new RunId(11), TestId.of(3));
        normalizer.onRunStarted(new RunId(20));
        normalizer.onRunFinished(new RunId(21));

        verify(target).onPrintedOut(new RunId(7), "stdout");
        verify(target).onPrintedErr(new RunId(8), "stderr");
        verify(target).onFailure(eq(new RunId(9)), notNull(StackTrace.class));
        verify(target).onTestStarted(new RunId(10), TestId.of(2));
        verify(target).onTestFinished(new RunId(11));
        verify(target).onRunStarted(new RunId(20), testFile);
        verify(target).onRunFinished(new RunId(21));
        verifyNoMoreInteractions(target);
    }

    @Test
    public void removes_duplicate_onTestFound_events() {
        normalizer.onTestFound(TestId.ROOT, "root");
        normalizer.onTestFound(TestId.ROOT, "root");

        verify(target, times(1)).onTestFound(testFile, TestId.ROOT, "root");
        verifyNoMoreInteractions(target);
    }

    @Test
    public void tests_must_be_found_always_with_the_same_name() {
        normalizer.onTestFound(TestId.ROOT, "first name");

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("test TestId() was already found with another name: first name");
        normalizer.onTestFound(TestId.ROOT, "second name");
    }

    @Test
    public void parents_must_be_found_before_their_children() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("parent of TestId(0) must be found first");
        normalizer.onTestFound(TestId.of(0), "child");
    }
}
