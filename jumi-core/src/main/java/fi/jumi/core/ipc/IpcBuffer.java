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

    private Segment segmentContaining(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException();
        }
        while (index < current.startInclusive) { // FIXME: off by one? works even with "-1"
            current = current.prev();
        }
        while (index >= current.endExclusive) {
            current = current.next();
        }
        return current;
    }

    // absolute get

    public byte getByte(int index) {
        Segment segment = segmentContaining(index);
        index = segment.relativize(index);
        return segment.buffer.get(index);
    }

    public short getShort(int index) {
        Segment segment = segmentContaining(index);
        index = segment.relativize(index);
        return segment.buffer.getShort(index);
    }

    public char getChar(int index) {
        Segment segment = segmentContaining(index);
        index = segment.relativize(index);
        return segment.buffer.getChar(index);
    }

    public int getInt(int index) {
        Segment segment = segmentContaining(index);
        index = segment.relativize(index);
        return segment.buffer.getInt(index);
    }

    public long getLong(int index) {
        Segment segment = segmentContaining(index);
        index = segment.relativize(index);
        return segment.buffer.getLong(index);
    }

    // absolute set

    public IpcBuffer setByte(int index, byte value) {
        Segment segment = segmentContaining(index);
        index = segment.relativize(index);
        segment.buffer.put(index, value);
        return this;
    }

    public IpcBuffer setShort(int index, short value) {
        Segment segment = segmentContaining(index);
        index = segment.relativize(index);
        segment.buffer.putShort(index, value);
        return this;
    }

    public IpcBuffer setChar(int index, char value) {
        Segment segment = segmentContaining(index);
        index = segment.relativize(index);
        segment.buffer.putChar(index, value);
        return this;
    }

    public IpcBuffer setInt(int index, int value) {
        Segment segment = segmentContaining(index);
        index = segment.relativize(index);
        segment.buffer.putInt(index, value);
        return this;
    }

    public IpcBuffer setLong(int index, long value) {
        Segment segment = segmentContaining(index);
        index = segment.relativize(index);
        segment.buffer.putLong(index, value);
        return this;
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
