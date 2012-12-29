// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.files;

import fi.jumi.actors.ActorRef;
import fi.jumi.core.util.ClassFiles;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.nio.file.*;

@NotThreadSafe
public class FileNamePatternTestClassFinder implements TestClassFinder {

    private final PathMatcher matcher;
    private final Path baseDir;
    private final ClassLoader classLoader;

    public FileNamePatternTestClassFinder(PathMatcher matcher, Path baseDir, ClassLoader classLoader) {
        this.matcher = matcher;
        this.baseDir = baseDir;
        this.classLoader = classLoader;
    }

    @Override
    public void findTestClasses(ActorRef<TestClassFinderListener> listener) {
        try {
            Files.walkFileTree(baseDir, new ClassFindingFileVisitor(matcher, baseDir, listener));
        } catch (IOException e) {
            throw new RuntimeException("Failed to traverse " + baseDir, e);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + matcher + ", " + baseDir + ", " + classLoader + ")";
    }

    @NotThreadSafe
    private class ClassFindingFileVisitor extends RelativePathMatchingFileVisitor {
        private final ActorRef<TestClassFinderListener> listener;

        public ClassFindingFileVisitor(PathMatcher matcher, Path baseDir, ActorRef<TestClassFinderListener> listener) {
            super(matcher, baseDir);
            this.listener = listener;
        }

        @Override
        protected void fileFound(Path relativePath) {
            // TODO: move the class loading out of this class?
            String className = ClassFiles.pathToClassName(relativePath);
            try {
                Class<?> testClass = classLoader.loadClass(className);
                listener.tell().onTestClassFound(testClass);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
