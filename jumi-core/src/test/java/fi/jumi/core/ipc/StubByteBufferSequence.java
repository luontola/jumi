// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import javax.annotation.concurrent.NotThreadSafe;
import java.nio.ByteBuffer;
import java.util.*;

@NotThreadSafe
public class StubByteBufferSequence implements ByteBufferSequence {

    private final List<ByteBuffer> segments = new ArrayList<>();

    public StubByteBufferSequence(int... segmentCapacities) {
        for (int capacity : segmentCapacities) {
            segments.add(ByteBuffer.allocate(capacity));
        }
    }

    @Override
    public ByteBuffer get(int index) {
        return segments.get(index);
    }
}
