// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import javax.annotation.concurrent.NotThreadSafe;
import java.nio.ByteBuffer;
import java.util.*;

@NotThreadSafe
public class IpcBuffer {

    private final ByteBufferSequence buffers;
    private int position = 0;
    private List<Segment> segments = new ArrayList<>();

    public IpcBuffer(ByteBufferSequence buffers) {
        this.buffers = buffers;
        segments.add(new Segment(0, buffers.get(0)));
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

        for (int i = 0; i < segments.size(); i++) {
            Segment segment = segments.get(i);
            if (index >= segment.startPosition && index < segment.startPosition + segment.buffer.capacity()) {
                return segment;
            } else if (i == segments.size() - 1) {
                segments.add(new Segment(segment.startPosition + segment.buffer.capacity(), buffers.get(i + 1)));
            }
        }

        throw new Error("This line should never be reached");
    }

    // absolute get

    public byte getByte(int index) {
        Segment segment = segmentContaining(index);
        return segment.buffer.get(index - segment.startPosition);
    }

    public short getShort(int index) {
        Segment segment = segmentContaining(index);
        return segment.buffer.getShort(index - segment.startPosition);
    }

    public char getChar(int index) {
        Segment segment = segmentContaining(index);
        return segment.buffer.getChar(index - segment.startPosition);
    }

    public int getInt(int index) {
        Segment segment = segmentContaining(index);
        return segment.buffer.getInt(index - segment.startPosition);
    }

    public long getLong(int index) {
        Segment segment = segmentContaining(index);
        return segment.buffer.getLong(index - segment.startPosition);
    }

    // absolute set

    public IpcBuffer setByte(int index, byte value) {
        Segment segment = segmentContaining(index);
        segment.buffer.put(index - segment.startPosition, value);
        return this;
    }

    public IpcBuffer setShort(int index, short value) {
        Segment segment = segmentContaining(index);
        segment.buffer.putShort(index - segment.startPosition, value);
        return this;
    }

    public IpcBuffer setChar(int index, char value) {
        Segment segment = segmentContaining(index);
        segment.buffer.putChar(index - segment.startPosition, value);
        return this;
    }

    public IpcBuffer setInt(int index, int value) {
        Segment segment = segmentContaining(index);
        segment.buffer.putInt(index - segment.startPosition, value);
        return this;
    }

    public IpcBuffer setLong(int index, long value) {
        Segment segment = segmentContaining(index);
        segment.buffer.putLong(index - segment.startPosition, value);
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
    private static class Segment {
        public final int startPosition;
        public final ByteBuffer buffer;

        public Segment(int startPosition, ByteBuffer buffer) {
            this.startPosition = startPosition;
            this.buffer = buffer;
        }
    }
}
