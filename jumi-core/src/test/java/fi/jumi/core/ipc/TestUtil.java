// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import fi.jumi.core.ipc.buffer.*;

public class TestUtil {

    public static <T> T serializeAndDeserialize(T original, WriteOp<T> writeOp, ReadOp<T> readOp) {
        IpcBuffer buffer = newIpcBuffer();
        writeOp.write(buffer, original);
        buffer.position(0);
        return readOp.read(buffer);
    }

    public static IpcBuffer newIpcBuffer() {
        return new IpcBuffer(new AllocatedByteBufferSequence(100, 30 * 1000));
    }

    public interface WriteOp<T> {
        void write(IpcBuffer target, T data);
    }

    public interface ReadOp<T> {
        T read(IpcBuffer source);
    }
}
