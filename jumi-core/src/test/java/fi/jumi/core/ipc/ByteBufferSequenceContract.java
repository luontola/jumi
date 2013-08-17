// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import org.junit.Test;

import java.nio.ByteBuffer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public abstract class ByteBufferSequenceContract {

    protected abstract ByteBufferSequence newByteBufferSequence();

    @Test
    public void different_segments_have_different_data() {
        ByteBufferSequence sequence = newByteBufferSequence();
        ByteBuffer segment0 = sequence.get(0);
        ByteBuffer segment1 = sequence.get(1);

        segment0.put(0, (byte) 10);
        segment1.put(0, (byte) 20);

        assertThat(segment0.get(0), is((byte) 10));
        assertThat(segment1.get(0), is((byte) 20));
    }

    @Test
    public void many_requests_for_same_segment_always_returns_same_data() {
        ByteBufferSequence sequence = newByteBufferSequence();

        sequence.get(0).put(0, (byte) 10);
        byte actual = sequence.get(0).get(0);

        assertThat(actual, is((byte) 10));
    }
}
