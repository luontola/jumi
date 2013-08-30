// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc.buffer;

import javax.annotation.concurrent.NotThreadSafe;
import java.nio.ByteBuffer;
import java.util.*;

@NotThreadSafe
public class FixedByteBufferSequence implements ByteBufferSequence {

    private final List<ByteBuffer> segments = new ArrayList<>();

    public FixedByteBufferSequence(int... segmentCapacities) {
        for (int capacity : segmentCapacities) {
            segments.add(ByteBuffer.allocate(capacity));
        }
    }

    public FixedByteBufferSequence(ByteBuffer... segments) {
        Collections.addAll(this.segments, segments);
    }

    @Override
    public ByteBuffer get(int index) {
        if (index >= segments.size()) {
            throw new IllegalArgumentException("tried to get segment at index " + index
                    + ", but there were only " + segments.size() + " segments");
        }
        return segments.get(index).duplicate();
    }

    public ByteBuffer combinedBuffer() {
        int capacity = 0;
        for (ByteBuffer segment : segments) {
            capacity += segment.capacity();
        }
        ByteBuffer combined = ByteBuffer.allocate(capacity);
        for (ByteBuffer segment : segments) {
            combined.put(segment.duplicate());
        }
        return combined;
    }
}
