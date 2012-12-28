// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.files;

import javax.annotation.concurrent.NotThreadSafe;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

@NotThreadSafe
public abstract class RelativePathMatchingFileVisitor extends SimpleFileVisitor<Path> {

    private final PathMatcher matcher;
    private final Path baseDir;

    public RelativePathMatchingFileVisitor(PathMatcher matcher, Path baseDir) {
        this.matcher = matcher;
        this.baseDir = baseDir;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        Path relativePath = baseDir.relativize(file);
        if (matcher.matches(relativePath)) {
            fileFound(relativePath);
        }
        return FileVisitResult.CONTINUE;
    }

    protected abstract void fileFound(Path relativePath);
}