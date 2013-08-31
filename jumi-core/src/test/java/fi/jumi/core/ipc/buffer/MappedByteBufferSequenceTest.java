// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc.buffer;

import org.junit.*;
import org.junit.rules.*;

import java.io.IOException;
import java.nio.*;
import java.nio.file.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.internal.matchers.ThrowableMessageMatcher.hasMessage;

public class MappedByteBufferSequenceTest extends ByteBufferSequenceContract {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Rule
    public final TemporaryFolder tempDir = new TemporaryFolder();

    @Override
    protected ByteBufferSequence newByteBufferSequence() {
        return MappedByteBufferSequence.readWrite(new FileSegmenter(getBasePath(), 10, 10));
    }

    private Path getBasePath() {
        return tempDir.getRoot().toPath().resolve("buffer");
    }


    @Test
    public void multiple_instances_and_processes_using_the_same_path_will_access_the_same_data() {
        // we test only multiple instances, but the basic idea of memory-mapped files is the same
        Path basePath = getBasePath();
        MappedByteBufferSequence sequence1 = MappedByteBufferSequence.readWrite(new FileSegmenter(basePath, 10, 10));
        MappedByteBufferSequence sequence2 = MappedByteBufferSequence.readWrite(new FileSegmenter(basePath, 10, 10));

        sequence1.get(0).put((byte) 123);
        byte b = sequence2.get(0).get();

        assertThat(b, is((byte) 123));
    }

    @Test
    public void if_file_exists_then_maps_the_whole_file_instead_of_what_the_default_segment_size_is() {
        Path basePath = getBasePath();

        MappedByteBufferSequence sequence1 = MappedByteBufferSequence.readWrite(new FileSegmenter(basePath, 10, 10));
        int capacity1 = sequence1.get(0).capacity();

        MappedByteBufferSequence sequence2 = MappedByteBufferSequence.readWrite(new FileSegmenter(basePath, 20, 20));
        int capacity2 = sequence2.get(0).capacity();

        assertThat("capacity of latter mapping", capacity2, is(capacity1));
    }

    @Test
    public void refuses_to_open_files_that_are_0_bytes_long() throws IOException {
        FileSegmenter segmenter = new FileSegmenter(getBasePath(), 10, 10);
        MappedByteBufferSequence sequence = MappedByteBufferSequence.readWrite(segmenter);

        Files.createFile(segmenter.pathOf(0));

        thrown.expectCause(instanceOf(IOException.class));
        thrown.expectCause(hasMessage(is("file size was 0 bytes")));
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("failed to map " + segmenter.pathOf(0));
        sequence.get(0);
    }

    @Test
    public void read_only_buffers_cannot_be_modified() {
        MappedByteBufferSequence sequence = MappedByteBufferSequence.readOnly(new FileSegmenter(getBasePath(), 10, 10));
        ByteBuffer buffer = sequence.get(0);

        buffer.get(); // can read, doesn't throw anything

        thrown.expect(ReadOnlyBufferException.class);
        buffer.put((byte) 1);
    }
}
