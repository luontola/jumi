// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.files;

import fi.jumi.actors.ActorRef;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.nio.file.*;

@NotThreadSafe
public class FileNamePatternTestClassFinder implements TestClassFinder {

    private final String syntaxAndPattern;
    private final Path classesDirectory;
    private final ClassLoader classLoader;

    public FileNamePatternTestClassFinder(String syntaxAndPattern, Path classesDirectory, ClassLoader classLoader) {
        this.syntaxAndPattern = syntaxAndPattern;
        this.classesDirectory = classesDirectory;
        this.classLoader = classLoader;
    }

    @Override
    public void findTestClasses(final ActorRef<TestClassFinderListener> listener) {
        final PathMatcher matcher = classesDirectory.getFileSystem().getPathMatcher(syntaxAndPattern);

        try {
            Files.walkFileTree(classesDirectory, new ClassFindingFileVisitor(matcher, classesDirectory, listener));
        } catch (IOException e) {
            throw new RuntimeException("Failed to traverse " + classesDirectory, e);
        }
    }

    private static String pathToClassName(Path path) {
        if (!path.toString().endsWith(".class")) {
            throw new IllegalArgumentException("Not a class file: " + path);
        }
        StringBuilder sb = new StringBuilder();
        for (Path p : path) {
            sb.append(p.getFileName());
            sb.append(".");
        }
        return sb.substring(0, sb.lastIndexOf(".class."));
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

    @NotThreadSafe
    private class ClassFindingFileVisitor extends RelativePathMatchingFileVisitor {
        private final ActorRef<TestClassFinderListener> listener;

        public ClassFindingFileVisitor(PathMatcher matcher, Path classesDirectory, ActorRef<TestClassFinderListener> listener) {
            super(matcher, classesDirectory);
            this.listener = listener;
        }

        @Override
        protected void fileFound(Path relativePath) {
            // TODO: move the class loading out of this class?
            String className = pathToClassName(relativePath);
            try {
                Class<?> testClass = classLoader.loadClass(className);
                listener.tell().onTestClassFound(testClass);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
