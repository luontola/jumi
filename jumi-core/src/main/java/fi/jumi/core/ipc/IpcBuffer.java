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
        while (index < current.startInclusive) {
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
        return segment.buffer.get(segment.relativize(index));
    }

    public short getShort(int index) {
        Segment segment = segmentContaining(index);
        if (index + 2 <= segment.endExclusive) {
            return segment.buffer.getShort(segment.relativize(index));
        } else {
            return (short) ((getByte(index) & 0xFF) << 8 | getByte(index + 1) & 0xFF);
        }
    }

    public char getChar(int index) {
        return (char) getShort(index);
    }

    public int getInt(int index) {
        Segment segment = segmentContaining(index);
        if (index + 4 <= segment.endExclusive) {
            return segment.buffer.getInt(segment.relativize(index));
        } else {
            return (getShort(index) & 0xFFFF) << 16 | getShort(index + 2) & 0xFFFF;
        }
    }

    public long getLong(int index) {
        Segment segment = segmentContaining(index);
        if (index + 8 <= segment.endExclusive) {
            return segment.buffer.getLong(segment.relativize(index));
        } else {
            return (getInt(index) & 0xFFFFFFFFL) << 32 | getInt(index + 4) & 0xFFFFFFFFL;
        }
    }

    // absolute set

    public IpcBuffer setByte(int index, byte value) {
        Segment segment = segmentContaining(index);
        segment.buffer.put(segment.relativize(index), value);
        return this;
    }

    public IpcBuffer setShort(int index, short value) {
        Segment segment = segmentContaining(index);
        if (index + 2 <= segment.endExclusive) {
            segment.buffer.putShort(segment.relativize(index), value);
        } else {
            setByte(index, (byte) (value >>> 8));
            setByte(index + 1, (byte) value);
        }
        return this;
    }

    public IpcBuffer setChar(int index, char value) {
        return setShort(index, (short) value);
    }

    public IpcBuffer setInt(int index, int value) {
        Segment segment = segmentContaining(index);
        if (index + 4 <= segment.endExclusive) {
            segment.buffer.putInt(segment.relativize(index), value);
        } else {
            setShort(index, (short) (value >>> 16));
            setShort(index + 2, (short) value);
        }
        return this;
    }

    public IpcBuffer setLong(int index, long value) {
        Segment segment = segmentContaining(index);
        if (index + 8 <= segment.endExclusive) {
            segment.buffer.putLong(segment.relativize(index), value);
        } else {
            setInt(index, (int) (value >>> 32));
            setInt(index + 4, (int) value);
        }
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
        return (char) readShort();
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
        return writeShort((short) value);
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
