// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.files;

import fi.jumi.actors.ActorRef;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.List;

@NotThreadSafe
public class CompositeTestClassFinder implements TestClassFinder {

    private final List<TestClassFinder> finders;

    public CompositeTestClassFinder(List<TestClassFinder> finders) {
        this.finders = finders;
    }

    @Override
    public void findTestClasses(ActorRef<TestClassFinderListener> listener) {
        for (TestClassFinder finder : finders) {
            finder.findTestClasses(listener);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CompositeTestClassFinder)) {
            return super.equals(obj);
        }
        CompositeTestClassFinder that = (CompositeTestClassFinder) obj;
        return this.finders.equals(that.finders);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + finders + ")";
    }
}
