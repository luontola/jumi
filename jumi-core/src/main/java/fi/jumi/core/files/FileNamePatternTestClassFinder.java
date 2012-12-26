// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.files;

import fi.jumi.actors.ActorRef;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class FileNamePatternTestClassFinder implements TestClassFinder {

    private final String syntaxAndPattern;
    private final ClassLoader classLoader;

    public FileNamePatternTestClassFinder(String syntaxAndPattern, ClassLoader classLoader) {
        this.syntaxAndPattern = syntaxAndPattern;
        this.classLoader = classLoader;
    }

    @Override
    public void findTestClasses(ActorRef<TestClassFinderListener> listener) {
        //  TODO
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FileNamePatternTestClassFinder)) {
            return false;
        }
        FileNamePatternTestClassFinder that = (FileNamePatternTestClassFinder) obj;
        return this.syntaxAndPattern.equals(that.syntaxAndPattern);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + syntaxAndPattern + ")";
    }
}
