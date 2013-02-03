// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.discovery;

import fi.jumi.actors.ActorRef;
import fi.jumi.core.api.TestFile;
import fi.jumi.core.util.SpyListener;
import org.junit.Test;

import java.util.Arrays;

import static org.mockito.Mockito.*;

public class CompositeTestFileFinderTest {

    @Test
    public void invokes_each_of_the_finders() {
        TestFileFinder finder1 = mock(TestFileFinder.class);
        TestFileFinder finder2 = mock(TestFileFinder.class);
        ActorRef<TestFileFinderListener> listenerRef = ActorRef.wrap(mock(TestFileFinderListener.class));

        CompositeTestFileFinder composite = new CompositeTestFileFinder(Arrays.asList(finder1, finder2));
        composite.findTestFiles(listenerRef);

        verify(finder1).findTestFiles(listenerRef);
        verify(finder2).findTestFiles(listenerRef);
    }

    @Test
    public void notifies_after_all_test_files_have_been_found() {
        SpyListener<TestFileFinderListener> spy = new SpyListener<>(TestFileFinderListener.class);
        TestFileFinderListener expect = spy.getListener();
        CompositeTestFileFinder composite = new CompositeTestFileFinder(Arrays.<TestFileFinder>asList(
                new FakeTestFileFinder(DummyTest1.class), new FakeTestFileFinder(DummyTest2.class)));

        expect.onTestFileFound(TestFile.fromClass(DummyTest1.class));
        expect.onTestFileFound(TestFile.fromClass(DummyTest2.class));
        expect.onAllTestFilesFound();

        spy.replay();
        composite.findTestFiles(ActorRef.wrap(expect));
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
        }
    }
}
