// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import fi.jumi.core.ipc.buffer.IpcBuffer;

import javax.annotation.concurrent.NotThreadSafe;
import java.net.*;
import java.util.*;

@NotThreadSafe
public abstract class EncodingUtil {

    protected final IpcBuffer buffer;

    protected EncodingUtil(IpcBuffer buffer) {
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

    protected void writeUriList(List<URI> uris) {
        writeList(uris, this::writeUri);
    }

    protected URI[] readUriList() {
        List<URI> uris = readList(this::readUri);
        return uris.toArray(new URI[uris.size()]);
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

    protected void writeStringList(List<String> strings) {
        writeList(strings, this::writeString);
    }

    protected String[] readStringList() {
        List<String> list = readList(this::readString);
        return list.toArray(new String[list.size()]);
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

    // List

    protected <T> void writeList(List<T> values, WriteOp<T> op) {
        buffer.writeInt(values.size());
        for (T value : values) {
            op.write(value);
        }
    }

    protected interface WriteOp<T> {
        void write(T value);
    }

    protected <T> List<T> readList(ReadOp<T> op) {
        int size = buffer.readInt();
        List<T> strings = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            strings.add(op.read());
        }
        return strings;
    }

    protected interface ReadOp<T> {
        T read();
    }
}
