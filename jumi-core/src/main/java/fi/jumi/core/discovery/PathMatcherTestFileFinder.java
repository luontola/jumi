// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.discovery;

import fi.jumi.actors.ActorRef;
import fi.jumi.core.api.TestFile;

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
    public void findTestFiles(final ActorRef<TestFileFinderListener> listener) {
        @NotThreadSafe
        class ListenerNotifyingFileVisitor extends RelativePathMatchingFileVisitor {
            public ListenerNotifyingFileVisitor(PathMatcher matcher, Path baseDir) {
                super(matcher, baseDir);
            }

            @Override
            protected void fileFound(Path relativePath) {
                listener.tell().onTestFileFound(TestFile.fromPath(relativePath));
            }
        }

        try {
            Files.walkFileTree(baseDir, new ListenerNotifyingFileVisitor(matcher, baseDir));
        } catch (IOException e) {
            throw new RuntimeException("Failed to traverse " + baseDir, e);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + matcher + ", " + baseDir + ")";
    }
}
