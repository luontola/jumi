// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.discovery;

import fi.jumi.actors.ActorRef;
import fi.jumi.core.api.TestFile;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.List;

@NotThreadSafe
public class CompositeTestFileFinder implements TestFileFinder {

    private final List<TestFileFinder> finders;

    public CompositeTestFileFinder(List<TestFileFinder> finders) {
        this.finders = finders;
    }

    @Override
    public void findTestFiles(ActorRef<TestFileFinderListener> listener) {
        TestFileFinderListener filter = new DuplicateOnAllTestFilesFoundEventFilter(finders.size(), listener);
        for (TestFileFinder finder : finders) {
            finder.findTestFiles(ActorRef.wrap(filter));
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + finders + ")";
    }


    @NotThreadSafe
    private static class DuplicateOnAllTestFilesFoundEventFilter implements TestFileFinderListener {

        private int count;
        private final ActorRef<TestFileFinderListener> listener;

        public DuplicateOnAllTestFilesFoundEventFilter(int count, ActorRef<TestFileFinderListener> listener) {
            this.count = count;
            this.listener = listener;
        }

        @Override
        public void onTestFileFound(TestFile testFile) {
            listener.tell().onTestFileFound(testFile);
        }

        @Override
        public void onAllTestFilesFound() {
            count--;
            if (count == 0) {
                listener.tell().onAllTestFilesFound();
            }
        }
    }
}
