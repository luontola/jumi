// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import fi.jumi.core.ipc.buffer.*;
import fi.jumi.core.ipc.channel.*;

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

    public static <T> void decodeAll(IpcReader<T> reader, T target) {
        // TODO: move to production sources?
        WaitStrategy waitStrategy = new ProgressiveSleepWaitStrategy();
        while (!Thread.interrupted()) {
            PollResult result = reader.poll(target);
            if (result == PollResult.NO_NEW_MESSAGES) {
                waitStrategy.await();
            }
            if (result == PollResult.HAD_SOME_MESSAGES) {
                waitStrategy.reset();
            }
            if (result == PollResult.END_OF_STREAM) {
                return;
            }
        }
    }

    public interface WriteOp<T> {
        void write(IpcBuffer target, T data);
    }

    public interface ReadOp<T> {
        T read(IpcBuffer source);
    }
}
