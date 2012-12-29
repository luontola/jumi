// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.testbench;

import fi.jumi.actors.ActorRef;
import fi.jumi.core.files.*;
import fi.jumi.core.util.ClassFiles;

import java.nio.file.Paths;

public class StubTestClassFinder implements TestClassFinder {

    private final Class<?>[] testClasses;

    public StubTestClassFinder(Class<?>... testClasses) {
        this.testClasses = testClasses;
    }

    @Override
    public void findTestClasses(ActorRef<TestClassFinderListener> listener) {
        for (Class<?> testClass : testClasses) {
            listener.tell().onTestClassFound(Paths.get(ClassFiles.classNameToPath(testClass.getName())));
        }
    }
}
