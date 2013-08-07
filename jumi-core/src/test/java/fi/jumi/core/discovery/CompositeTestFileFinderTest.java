// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.discovery;

import fi.jumi.actors.ActorRef;
import fi.jumi.core.api.TestFile;
import fi.jumi.core.util.SpyListener;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.mockito.Matchers;

import java.util.*;

import static com.googlecode.catchexception.CatchException.*;
import static com.googlecode.catchexception.apis.CatchExceptionHamcrestMatchers.hasMessage;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class CompositeTestFileFinderTest {

    private final SpyListener<TestFileFinderListener> spy = new SpyListener<>(TestFileFinderListener.class);
    private final TestFileFinderListener expect = spy.getListener();

    @Test
    public void invokes_each_of_the_finders_and_notifies_once_after_all_of_them_are_finished() {
        CompositeTestFileFinder composite = new CompositeTestFileFinder(Arrays.<TestFileFinder>asList(
                new FakeTestFileFinder(DummyTest1.class),
                new FakeTestFileFinder(DummyTest2.class)
        ));

        expect.onTestFileFound(TestFile.fromClass(DummyTest1.class));
        expect.onTestFileFound(TestFile.fromClass(DummyTest2.class));
        expect.onAllTestFilesFound();

        spy.replay();
        composite.findTestFiles(ActorRef.wrap(expect));
        spy.verify();
    }

    @Test
    public void zero_finders() {
        CompositeTestFileFinder composite = new CompositeTestFileFinder(new ArrayList<TestFileFinder>());

        expect.onAllTestFilesFound();

        spy.replay();
        composite.findTestFiles(ActorRef.wrap(expect));
        spy.verify();
    }

    @Test
    public void buggy_finders() {
        TestFileFinder buggyFinder = mock(TestFileFinder.class);
        doThrow(new RuntimeException("dummy exception")).when(buggyFinder).findTestFiles(Matchers.<ActorRef<TestFileFinderListener>>any());
        CompositeTestFileFinder composite = new CompositeTestFileFinder(Arrays.asList(buggyFinder));

        expect.onAllTestFilesFound();

        spy.replay();
        catchException(composite).findTestFiles(ActorRef.wrap(expect));
        assertThat(caughtException(), (Matcher) hasMessage("dummy exception"));
        spy.verify();
    }


    private static class DummyTest1 {
    }

    private static class DummyTest2 {
    }

    private static class FakeTestFileFinder implements TestFileFinder {
        private final Class<?> testClass;

        private FakeTestFileFinder(Class<?> testClass) {
            this.testClass = testClass;
        }

        @Override
        public void findTestFiles(ActorRef<TestFileFinderListener> listener) {
            listener.tell().onTestFileFound(TestFile.fromClass(testClass));
            listener.tell().onAllTestFilesFound();
        }
    }
}
