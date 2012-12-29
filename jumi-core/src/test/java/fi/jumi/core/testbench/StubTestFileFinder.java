// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.testbench;

import fi.jumi.actors.ActorRef;
import fi.jumi.core.files.*;
import fi.jumi.core.util.ClassFiles;

import java.nio.file.Paths;

public class StubTestFileFinder implements TestFileFinder {

    private final Class<?>[] testClasses;

    public StubTestFileFinder(Class<?>... testClasses) {
        this.testClasses = testClasses;
    }

    @Override
    public void findTestFiles(ActorRef<TestFileFinderListener> listener) {
        for (Class<?> testClass : testClasses) {
            listener.tell().onTestFileFound(Paths.get(ClassFiles.classNameToPath(testClass.getName())));
        }
    }
}
