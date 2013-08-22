// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import org.junit.*;
import org.junit.rules.ExpectedException;

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
}
