// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc.buffer;

import javax.annotation.concurrent.NotThreadSafe;
import java.nio.ByteBuffer;
import java.util.*;

@NotThreadSafe
public class AllocatedByteBufferSequence implements ByteBufferSequence {

    private final List<ByteBuffer> segments = new ArrayList<>();
    private final int segmentCapacity;
    private final int maxSegments;

    public AllocatedByteBufferSequence(int segmentCapacity) {
        this(segmentCapacity, Integer.MAX_VALUE);
    }

    public AllocatedByteBufferSequence(int segmentCapacity, int totalCapacityLimit) {
        this.segmentCapacity = segmentCapacity;
        this.maxSegments = totalCapacityLimit / segmentCapacity;
    }

    @Override
    public ByteBuffer get(int index) {
        if (index >= maxSegments) {
            throw new IllegalArgumentException("tried to get segment at index " + index +
                    ", but there were only " + maxSegments + " segments");
        }
        while (segments.size() <= index) {
            segments.add(ByteBuffer.allocate(segmentCapacity));
        }
        return segments.get(index).duplicate();
    }
}
