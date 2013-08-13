// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import javax.annotation.concurrent.NotThreadSafe;
import java.nio.ByteBuffer;

@NotThreadSafe
public class IpcBuffer {

    private final ByteBuffer buffer = ByteBuffer.allocate(100);

    public byte getByte(int index) {
        return buffer.get(index);
    }

    public IpcBuffer setByte(int index, byte b) {
        buffer.put(index, b);
        return this;
    }

    public byte readByte() {
        return buffer.get();
    }

    public IpcBuffer writeByte(byte b) {
        buffer.put(b);
        return this;
    }

    public IpcBuffer position(int newPosition) {
        buffer.position(newPosition);
        return this;
    }
}
