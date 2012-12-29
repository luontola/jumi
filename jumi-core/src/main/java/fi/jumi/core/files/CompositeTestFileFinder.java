// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.files;

import fi.jumi.actors.ActorRef;

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
        for (TestFileFinder finder : finders) {
            finder.findTestFiles(listener);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + finders + ")";
    }
}
