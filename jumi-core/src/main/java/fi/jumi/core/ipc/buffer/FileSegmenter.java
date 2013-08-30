// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc.buffer;

import javax.annotation.concurrent.Immutable;
import java.nio.file.Path;

@Immutable
public class FileSegmenter {

    private final Path base;
    private final int initialSize;
    private final int maxSize;

    public FileSegmenter(Path base, int initialSize, int maxSize) {
        if (maxSize < initialSize) {
            throw new IllegalArgumentException("max size " + maxSize + " was less than initial size " + initialSize);
        }
        this.base = base;
        this.initialSize = initialSize;
        this.maxSize = maxSize;
    }

    public Path pathOf(int segment) {
        if (segment == 0) {
            return base;
        }
        String suffix = String.format(".%03d", segment);
        return base.resolveSibling(base.getFileName() + suffix);
    }

    public int sizeOf(int segment) {
        int size = initialSize;
        for (int i = 0; i < segment; i++) {
            size *= 2;
            if (size > maxSize) {
                return maxSize;
            }
        }
        return size;
    }
}
