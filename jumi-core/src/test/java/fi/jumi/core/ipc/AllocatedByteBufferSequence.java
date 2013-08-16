// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import javax.annotation.concurrent.NotThreadSafe;
import java.nio.ByteBuffer;
import java.util.*;

@NotThreadSafe
public class AllocatedByteBufferSequence implements ByteBufferSequence {

    private final List<ByteBuffer> segments = new ArrayList<>();
    private final int segmentCapacity;

    public AllocatedByteBufferSequence(int segmentCapacity) {
        this.segmentCapacity = segmentCapacity;
    }

    @Override
    public ByteBuffer get(int index) {
        while (segments.size() <= index) {
            segments.add(ByteBuffer.allocate(segmentCapacity));
        }
        return segments.get(index);
    }
}
