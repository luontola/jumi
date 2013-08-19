// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class MappedByteBufferSequenceTest extends ByteBufferSequenceContract {

    @Rule
    public final TemporaryFolder tempDir = new TemporaryFolder();

    @Override
    protected ByteBufferSequence newByteBufferSequence() {
        return new MappedByteBufferSequence(new FileSegmenter(getBasePath(), 10, 10));
    }

    private Path getBasePath() {
        return tempDir.getRoot().toPath().resolve("buffer");
    }


    @Test
    public void multiple_instances_and_processes_using_the_same_path_will_access_the_same_data() {
        // we test only multiple instances, but the basic idea of memory-mapped files is the same
        Path basePath = getBasePath();
        MappedByteBufferSequence buffer1 = new MappedByteBufferSequence(new FileSegmenter(basePath, 10, 10));
        MappedByteBufferSequence buffer2 = new MappedByteBufferSequence(new FileSegmenter(basePath, 10, 10));

        buffer1.get(0).put((byte) 123);
        byte b = buffer2.get(0).get();

        assertThat(b, is((byte) 123));
    }
}
