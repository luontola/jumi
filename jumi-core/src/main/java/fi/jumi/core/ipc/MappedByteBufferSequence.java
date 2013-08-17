// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.*;

@NotThreadSafe
public class MappedByteBufferSequence implements ByteBufferSequence {

    private final Path basePath;
    private final int segmentCapacity;

    public MappedByteBufferSequence(Path basePath, int segmentCapacity) {
        this.basePath = basePath;
        this.segmentCapacity = segmentCapacity;
    }

    @Override
    public ByteBuffer get(int index) {
        try (FileChannel fc = FileChannel.open(getSegmentPath(index), READ, WRITE, CREATE)) {
            return fc.map(FileChannel.MapMode.READ_WRITE, 0, segmentCapacity);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Path getSegmentPath(int index) {
        String name = basePath.getFileName().toString() + "." + index;
        return basePath.resolveSibling(name);
    }
}
