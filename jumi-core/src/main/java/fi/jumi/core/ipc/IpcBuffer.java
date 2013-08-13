// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import javax.annotation.concurrent.NotThreadSafe;
import java.nio.ByteBuffer;

@NotThreadSafe
public class IpcBuffer {

    private final ByteBuffer buffer = ByteBuffer.allocate(100);

    public IpcBuffer position(int newPosition) {
        buffer.position(newPosition);
        return this;
    }

    // absolute get

    public byte getByte(int index) {
        return buffer.get(index);
    }

    public short getShort(int index) {
        return buffer.getShort(index);
    }

    public char getChar(int index) {
        return buffer.getChar(index);
    }

    public int getInt(int index) {
        return buffer.getInt(index);
    }

    public long getLong(int index) {
        return buffer.getLong(index);
    }

    // absolute set

    public IpcBuffer setByte(int index, byte value) {
        buffer.put(index, value);
        return this;
    }

    public IpcBuffer setShort(int index, short value) {
        buffer.putShort(index, value);
        return this;
    }

    public IpcBuffer setChar(int index, char value) {
        buffer.putChar(index, value);
        return this;
    }

    public IpcBuffer setInt(int index, int value) {
        buffer.putInt(index, value);
        return this;
    }

    public IpcBuffer setLong(int index, long value) {
        buffer.putLong(index, value);
        return this;
    }

    // relative read

    public byte readByte() {
        return buffer.get();
    }

    public short readShort() {
        return buffer.getShort();
    }

    public char readChar() {
        return buffer.getChar();
    }

    public int readInt() {
        return buffer.getInt();
    }

    public long readLong() {
        return buffer.getLong();
    }

    // relative write

    public IpcBuffer writeByte(byte value) {
        buffer.put(value);
        return this;
    }

    public IpcBuffer writeShort(short value) {
        buffer.putShort(value);
        return this;
    }

    public IpcBuffer writeChar(char value) {
        buffer.putChar(value);
        return this;
    }

    public IpcBuffer writeInt(int value) {
        buffer.putInt(value);
        return this;
    }

    public IpcBuffer writeLong(long value) {
        buffer.putLong(value);
        return this;
    }
}
