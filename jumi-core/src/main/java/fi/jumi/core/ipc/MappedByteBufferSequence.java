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

    private final FileSegmenter segmenter;

    public MappedByteBufferSequence(FileSegmenter segmenter) {
        this.segmenter = segmenter;
    }

    @Override
    public ByteBuffer get(int index) {
        Path path = segmenter.pathOf(index);
        int size = segmenter.sizeOf(index);
        try (FileChannel fc = FileChannel.open(path, READ, WRITE, CREATE)) {
            return fc.map(FileChannel.MapMode.READ_WRITE, 0, size);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
