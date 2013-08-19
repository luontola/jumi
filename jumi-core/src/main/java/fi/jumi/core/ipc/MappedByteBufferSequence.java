// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import fi.jumi.core.util.Resilient;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.nio.file.*;

import static java.nio.file.StandardOpenOption.*;

@NotThreadSafe
public class MappedByteBufferSequence implements ByteBufferSequence {

    private final FileSegmenter segmenter;

    public MappedByteBufferSequence(FileSegmenter segmenter) {
        this.segmenter = segmenter;
    }

    @Override
    public ByteBuffer get(int index) {
        Path path = segmenter.pathOf(index);
        long size = segmenter.sizeOf(index);
        try {
            return Resilient.tryRepeatedly(() -> tryMapFile(path, size));
        } catch (IOException e) {
            throw new RuntimeException("failed to map " + path, e);
        }
    }

    private static MappedByteBuffer tryMapFile(Path path, long size) throws IOException {
        if (Files.exists(path)) {
            size = Files.size(path);
        }
        try (FileChannel fc = FileChannel.open(path, READ, WRITE, CREATE)) {
            return fc.map(FileChannel.MapMode.READ_WRITE, 0, size);
        }
    }
}
