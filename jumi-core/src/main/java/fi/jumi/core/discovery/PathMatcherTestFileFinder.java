// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.discovery;

import fi.jumi.actors.ActorRef;
import fi.jumi.core.api.TestFile;
import fi.jumi.core.util.Boilerplate;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.nio.file.*;

@NotThreadSafe
public class PathMatcherTestFileFinder implements TestFileFinder {

    private final PathMatcher matcher;
    private final Path baseDir;

    public PathMatcherTestFileFinder(PathMatcher matcher, Path baseDir) {
        this.matcher = matcher;
        this.baseDir = baseDir;
    }

    @Override
    public void findTestFiles(ActorRef<TestFileFinderListener> listener) {
        try {
            // TODO: Java 8, use Files.walk
            Files.walkFileTree(baseDir, new RelativePathMatchingFileVisitor(matcher, baseDir) {
                @Override
                protected void fileFound(Path relativePath) {
                    // XXX: This class must not call onAllTestFilesFound, but only CompositeTestFileFinder does, to avoid duplicate calls
                    listener.tell().onTestFileFound(TestFile.fromPath(relativePath));
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Failed to traverse " + baseDir, e);
        } finally {
            listener.tell().onAllTestFilesFound();
        }
    }

    @Override
    public String toString() {
        return Boilerplate.toString(getClass(), matcher, baseDir);
    }
}
