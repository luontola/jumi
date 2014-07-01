// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import fi.jumi.core.ipc.buffer.IpcBuffer;

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

    // String

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
}
