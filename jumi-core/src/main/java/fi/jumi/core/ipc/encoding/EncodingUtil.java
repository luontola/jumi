// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc.encoding;

import fi.jumi.core.ipc.buffer.IpcBuffer;

import javax.annotation.concurrent.NotThreadSafe;
import java.net.*;
import java.util.*;

@NotThreadSafe
public abstract class EncodingUtil {

    protected final IpcBuffer buffer;

    public EncodingUtil(IpcBuffer buffer) {
        this.buffer = buffer;
    }

    // event type

    protected byte readEventType() {
        return buffer.readByte();
    }

    protected void writeEventType(byte type) {
        buffer.writeByte(type);
    }

    // URI

    protected void writeUris(List<URI> uris) {
        writeList(uris, this::writeUri);
    }

    protected URI[] readUris() {
        return readArray(this::readUri, URI[]::new);
    }

    protected void writeUri(URI uri) {
        writeString(uri.toString());
    }

    protected URI readUri() {
        try {
            return new URI(readString());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    // String

    protected void writeStrings(List<String> strings) {
        writeList(strings, this::writeString);
    }

    protected String[] readStrings() {
        return readArray(this::readString, String[]::new);
    }

    protected String readString() {
        return StringEncoding.readString(buffer);
    }

    protected void writeString(String s) {
        StringEncoding.writeString(buffer, s);
    }

    protected String readNullableString() {
        return StringEncoding.readNullableString(buffer);
    }

    protected void writeNullableString(String s) {
        StringEncoding.writeNullableString(buffer, s);
    }

    // Collections

    protected <T> void writeArray(T[] values, WriteOp<T> writer) {
        writeList(Arrays.asList(values), writer);
    }

    protected <T> void writeList(List<T> values, WriteOp<T> writer) {
        buffer.writeInt(values.size());
        for (T value : values) {
            writer.write(value);
        }
    }

    protected <T> T[] readArray(ReadOp<T> reader, ArrayFactory<T> arrayFactory) {
        T[] values = arrayFactory.create(buffer.readInt());
        for (int i = 0; i < values.length; i++) {
            values[i] = reader.read();
        }
        return values;
    }

    protected void writeIntArray(int[] values) {
        buffer.writeInt(values.length);
        for (int value : values) {
            buffer.writeInt(value);
        }
    }

    protected int[] readIntArray() {
        int[] values = new int[buffer.readInt()];
        for (int i = 0; i < values.length; i++) {
            values[i] = buffer.readInt();
        }
        return values;
    }

    protected interface WriteOp<T> {
        void write(T value);
    }

    protected interface ReadOp<T> {
        T read();
    }

    protected interface ArrayFactory<T> {
        T[] create(int length);
    }
}
