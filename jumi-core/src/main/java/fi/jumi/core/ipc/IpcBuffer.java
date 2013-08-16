// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import javax.annotation.concurrent.NotThreadSafe;
import java.nio.*;

@NotThreadSafe
public class IpcBuffer {

    private final ByteBufferSequence buffers;
    private int position = 0;
    private Segment current;

    public IpcBuffer(ByteBufferSequence buffers) {
        this.buffers = buffers;
        current = new Segment();
    }

    public IpcBuffer position(int newPosition) {
        if (newPosition < 0) {
            throw new IllegalArgumentException();
        }
        position = newPosition;
        return this;
    }

    // absolute get

    public byte getByte(int index) {
        return get(index, ByteBuffer::get);
    }

    public short getShort(int index) {
        return get(index, ByteBuffer::getShort);
    }

    public char getChar(int index) {
        return get(index, ByteBuffer::getChar);
    }

    public int getInt(int index) {
        return get(index, ByteBuffer::getInt);
    }

    public long getLong(int index) {
        return get(index, ByteBuffer::getLong);
    }

    // absolute set

    public IpcBuffer setByte(int index, byte value) {
        return set(index, value, ByteBuffer::put);
    }

    public IpcBuffer setShort(int index, short value) {
        return set(index, value, ByteBuffer::putShort);
    }

    public IpcBuffer setChar(int index, char value) {
        return set(index, value, ByteBuffer::putChar);
    }

    public IpcBuffer setInt(int index, int value) {
        return set(index, value, ByteBuffer::putInt);
    }

    public IpcBuffer setLong(int index, long value) {
        return set(index, value, ByteBuffer::putLong);
    }

    // relative read

    public byte readByte() {
        byte value = getByte(position);
        position += 1;
        return value;
    }

    public short readShort() {
        short value = getShort(position);
        position += 2;
        return value;
    }

    public char readChar() {
        char value = getChar(position);
        position += 2;
        return value;
    }

    public int readInt() {
        int value = getInt(position);
        position += 4;
        return value;
    }

    public long readLong() {
        long value = getLong(position);
        position += 8;
        return value;
    }

    // relative write

    public IpcBuffer writeByte(byte value) {
        setByte(position, value);
        position += 1;
        return this;
    }

    public IpcBuffer writeShort(short value) {
        setShort(position, value);
        position += 2;
        return this;
    }

    public IpcBuffer writeChar(char value) {
        setChar(position, value);
        position += 2;
        return this;
    }

    public IpcBuffer writeInt(int value) {
        setInt(position, value);
        position += 4;
        return this;
    }

    public IpcBuffer writeLong(long value) {
        setLong(position, value);
        position += 8;
        return this;
    }


    // accessing segments

    private <T> T get(int absIndex, Getter<T> op) {
        Segment segment = segmentContaining(absIndex);
        int relIndex = segment.relativize(absIndex);
        return op.get(segment.buffer, relIndex);
    }

    private <T> IpcBuffer set(int absIndex, T value, Setter<T> op) {
        Segment segment = segmentContaining(absIndex);
        int relIndex = segment.relativize(absIndex);
        op.set(segment.buffer, relIndex, value);
        return this;
    }

    private Segment segmentContaining(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException();
        }
        while (index < current.startInclusive) {
            current = current.prev();
        }
        while (index >= current.endExclusive) {
            current = current.next();
        }
        return current;
    }

    private interface Getter<T> {
        T get(ByteBuffer buffer, int index);
    }

    private interface Setter<T> {
        void set(ByteBuffer buffer, int index, T value);
    }

    @NotThreadSafe
    private class Segment {
        public final Segment prev;
        public Segment next;

        private final int segmentIndex;
        public final ByteBuffer buffer;
        public final int startInclusive;
        public final int endExclusive;

        public Segment() {
            prev = null;
            segmentIndex = 0;
            buffer = buffers.get(segmentIndex);
            startInclusive = 0;
            endExclusive = buffer.capacity();
        }

        public Segment(Segment prev) {
            this.prev = prev;
            segmentIndex = prev.segmentIndex + 1;
            buffer = buffers.get(segmentIndex);
            startInclusive = prev.endExclusive;
            endExclusive = startInclusive + buffer.capacity();
        }

        private int relativize(int index) {
            return index - startInclusive;
        }

        public Segment prev() {
            if (prev == null) {
                throw new BufferUnderflowException();
            }
            return prev;
        }

        public Segment next() {
            if (next == null) {
                next = new Segment(this);
            }
            return next;
        }
    }
}
