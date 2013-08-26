// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.serialization;

import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.queue.MessageSender;
import fi.jumi.core.api.*;
import fi.jumi.core.events.SuiteListenerEventizer;
import fi.jumi.core.ipc.IpcBuffer;
import fi.jumi.core.util.MemoryBarrier;

import javax.annotation.concurrent.NotThreadSafe;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@NotThreadSafe
public class SuiteEventSerializer implements MessageSender<Event<SuiteListener>> {

    private static final byte[] HEADER_MAGIC_BYTES = "Jumi".getBytes(StandardCharsets.US_ASCII);
    private static final int PROTOCOL_VERSION = 1;

    private static final byte STATUS_EMPTY = 0;
    private static final byte STATUS_EXISTS = 1;
    private static final byte STATUS_END_OF_STREAM = 2;

    private final MemoryBarrier memoryBarrier = new MemoryBarrier();
    private final IpcBuffer target;
    private final SuiteListenerEncoding encoding;

    public SuiteEventSerializer(IpcBuffer target) {
        this.target = target;
        encoding = new SuiteListenerEncoding(target);
    }

    public void start() {
        writeHeader();
    }

    public void end() {
        writeStatusEndOfStream();
    }

    public SuiteListener sender() {
        return new SuiteListenerEventizer().newFrontend(this);
    }

    @Override
    public void send(Event<SuiteListener> message) {
        int index = writeStatusEmpty();
        encoding.serialize(message);
        setStatusExists(index);
    }

    public static void deserialize(IpcBuffer source, SuiteListener target) {
        readHeader(source);

        while (true) {
            // TODO: create proper waiting util
            // TODO: we should do a read barrier here
            int index = source.position();
            byte status = readStatus(source);
            if (status == STATUS_EMPTY) {
                source.position(index);
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                continue;
            } else if (status == STATUS_END_OF_STREAM) {
                return;
            } else {
                assert status == STATUS_EXISTS : status;
            }

            SuiteListenerEncoding.deserialize(source, target);
        }
    }

    private void writeHeader() {
        // first byte is zero to signify that the whole header has not yet been written
        target.writeByte((byte) 0);
        for (int i = 1; i < HEADER_MAGIC_BYTES.length; i++) {
            target.writeByte(HEADER_MAGIC_BYTES[i]);
        }

        target.writeInt(PROTOCOL_VERSION);
        writeString(SuiteListenerEncoding.INTERFACE_NAME);
        target.writeInt(SuiteListenerEncoding.INTERFACE_VERSION);

        // all done
        memoryBarrier.storeStore();
        target.setByte(0, HEADER_MAGIC_BYTES[0]);
    }

    private static void readHeader(IpcBuffer source) {
        waitUntilNonZero(source, 0);

        checkMagicBytes(source);
        checkProtocolVersion(source);
        checkInterface(source);
        checkInterfaceVersion(source);
    }

    private static void waitUntilNonZero(IpcBuffer source, int index) {
        // TODO: create proper waiting util
        // TODO: we should do a read barrier here
        while (source.getByte(index) == 0) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private static void checkMagicBytes(IpcBuffer source) {
        byte[] actual = new byte[HEADER_MAGIC_BYTES.length];
        for (int i = 0; i < actual.length; i++) {
            actual[i] = source.readByte();
        }
        if (!Arrays.equals(actual, HEADER_MAGIC_BYTES)) {
            throw new IllegalArgumentException("wrong header: expected " + format(HEADER_MAGIC_BYTES) + " but was " + format(actual));
        }
    }

    private static String format(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    private static void checkProtocolVersion(IpcBuffer source) {
        int actual = source.readInt();
        if (actual != PROTOCOL_VERSION) {
            throw new IllegalArgumentException("unsupported protocol version: " + actual);
        }
    }

    private static void checkInterface(IpcBuffer source) {
        String actual = readString(source);
        if (!actual.equals(SuiteListenerEncoding.INTERFACE_NAME)) {
            throw new IllegalArgumentException("wrong interface: expected " + SuiteListenerEncoding.INTERFACE_NAME + " but was " + actual);
        }
    }

    private static void checkInterfaceVersion(IpcBuffer source) {
        int actual = source.readInt();
        if (actual != SuiteListenerEncoding.INTERFACE_VERSION) {
            throw new IllegalArgumentException("unsupported interface version: " + actual);
        }
    }


    // event write status

    private static byte readStatus(IpcBuffer source) {
        return source.readByte();
    }

    private int writeStatusEmpty() {
        int index = target.position();
        target.writeByte(STATUS_EMPTY);
        return index;
    }

    private void setStatusExists(int index) {
        target.setByte(index, STATUS_EXISTS);
    }

    private void writeStatusEndOfStream() {
        target.writeByte(STATUS_END_OF_STREAM);
    }


    // StackTrace

    static StackTrace readStackTrace(IpcBuffer source) {
        return SuiteListenerEncoding.readStackTrace(source);
    }

    void writeStackTrace(StackTrace stackTrace) {
        encoding.writeStackTrace(stackTrace);
    }

    // String

    static String readString(IpcBuffer source) {
        return SuiteListenerEncoding.readString(source);
    }

    static String readNullableString(IpcBuffer source) {
        return SuiteListenerEncoding.readNullableString(source);
    }

    void writeString(String s) {
        encoding.writeString(s);
    }

    void writeNullableString(String s) {
        encoding.writeNullableString(s);
    }
}
