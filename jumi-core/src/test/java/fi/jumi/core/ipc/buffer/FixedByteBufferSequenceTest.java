// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc.buffer;

import org.junit.*;
import org.junit.rules.ExpectedException;

import java.nio.ByteBuffer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FixedByteBufferSequenceTest extends ByteBufferSequenceContract {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Override
    protected ByteBufferSequence newByteBufferSequence() {
        return new FixedByteBufferSequence(10, 10);
    }

    @Test
    public void cannot_access_more_segments_than_the_buffer_contains() {
        FixedByteBufferSequence sequence = new FixedByteBufferSequence(10, 10);

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("tried to get segment at index 2, but there were only 2 segments");
        sequence.get(2);
    }

    @Test
    public void can_combine_all_segments_into_one_ByteBuffer() {
        FixedByteBufferSequence sequence = new FixedByteBufferSequence(2, 2);

        sequence.get(0).put((byte) 1).put((byte) 2);
        sequence.get(1).put((byte) 3).put((byte) 4);

        ByteBuffer expected = ByteBuffer.allocate(4)
                .put((byte) 1)
                .put((byte) 2)
                .put((byte) 3)
                .put((byte) 4);
        assertThat(sequence.combinedBuffer(), is(expected));
        assertThat("should not change the underlying buffer's position", sequence.get(0).position(), is(0));
    }
}
