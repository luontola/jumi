// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import org.junit.*;
import org.junit.rules.ExpectedException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class AllocatedByteBufferSequenceTest extends ByteBufferSequenceContract {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Override
    protected ByteBufferSequence newByteBufferSequence() {
        return new AllocatedByteBufferSequence(10);
    }

    @Test
    public void each_segment_has_the_specified_capacity() {
        AllocatedByteBufferSequence sequence = new AllocatedByteBufferSequence(42);

        assertThat(sequence.get(0).capacity(), is(42));
        assertThat(sequence.get(1).capacity(), is(42));
    }

    @Test
    public void can_specify_total_capacity_limit() {
        AllocatedByteBufferSequence sequence = new AllocatedByteBufferSequence(10, 50);

        sequence.get(4);

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("tried to get segment at index 5, but there were only 5 segments");
        sequence.get(5);
    }
}
