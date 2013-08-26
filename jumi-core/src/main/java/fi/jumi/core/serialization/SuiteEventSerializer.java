// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.serialization;

import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.queue.MessageSender;
import fi.jumi.core.ipc.IpcBuffer;
import fi.jumi.core.util.MemoryBarrier;

import javax.annotation.concurrent.NotThreadSafe;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static fi.jumi.core.serialization.StringEncoding.*;

@NotThreadSafe
public class SuiteEventSerializer<T> implements MessageSender<Event<T>> {

    private static final byte[] HEADER_MAGIC_BYTES = "Jumi".getBytes(StandardCharsets.US_ASCII);
    private static final int PROTOCOL_VERSION = 1;

    private static final byte STATUS_EMPTY = 0;
    private static final byte STATUS_EXISTS = 1;
    private static final byte STATUS_END_OF_STREAM = 2;

    private final MemoryBarrier memoryBarrier = new MemoryBarrier();
    private final IpcBuffer buffer;
    private final MessageEncoding<T> messageEncoding;

    public SuiteEventSerializer(IpcBuffer buffer, EncodingFactory<T> encodingFactory) {
        this.buffer = buffer;
        this.messageEncoding = encodingFactory.create(buffer);
    }

    public void start() {
        writeHeader();
    }

    public void end() {
        writeStatusEndOfStream();
    }

    @Override
    public void send(Event<T> message) {
        int index = writeStatusEmpty();
        messageEncoding.encode(message);
        setStatusExists(index);
    }

    public void deserialize(T target) {
        readHeader();

        while (true) {
            // TODO: create proper waiting util
            // TODO: we should do a read barrier here
            int index = buffer.position();
            byte status = readStatus();
            if (status == STATUS_EMPTY) {
                buffer.position(index);
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

            messageEncoding.decode(target);
        }
    }


    // header

    private void writeHeader() {
        // first byte is zero to signify that the whole header has not yet been written
        buffer.writeByte((byte) 0);
        for (int i = 1; i < HEADER_MAGIC_BYTES.length; i++) {
            buffer.writeByte(HEADER_MAGIC_BYTES[i]);
        }

        buffer.writeInt(PROTOCOL_VERSION);
        writeString(buffer, messageEncoding.getInterfaceName());
        buffer.writeInt(messageEncoding.getInterfaceVersion());

        // all done
        memoryBarrier.storeStore();
        buffer.setByte(0, HEADER_MAGIC_BYTES[0]);
    }

    private void readHeader() {
        waitUntilNonZero(buffer, 0);

        checkMagicBytes();
        checkProtocolVersion();
        checkInterface();
        checkInterfaceVersion();
    }

    private void waitUntilNonZero(IpcBuffer buffer, int index) {
        // TODO: create proper waiting util
        // TODO: we should do a read barrier here
        while (buffer.getByte(index) == 0) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void checkMagicBytes() {
        byte[] actual = new byte[HEADER_MAGIC_BYTES.length];
        for (int i = 0; i < actual.length; i++) {
            actual[i] = buffer.readByte();
        }
        if (!Arrays.equals(actual, HEADER_MAGIC_BYTES)) {
            throw new IllegalArgumentException("wrong header: expected " + format(HEADER_MAGIC_BYTES) + " but was " + format(actual));
        }
    }

    private String format(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    private void checkProtocolVersion() {
        int actual = buffer.readInt();
        if (actual != PROTOCOL_VERSION) {
            throw new IllegalArgumentException("unsupported protocol version: " + actual);
        }
    }

    private void checkInterface() {
        String actual = readString(buffer);
        if (!actual.equals(messageEncoding.getInterfaceName())) {
            throw new IllegalArgumentException("wrong interface: expected " + messageEncoding.getInterfaceName() + " but was " + actual);
        }
    }

    private void checkInterfaceVersion() {
        int actual = buffer.readInt();
        if (actual != messageEncoding.getInterfaceVersion()) {
            throw new IllegalArgumentException("unsupported interface version: " + actual);
        }
    }


    // messages

    private byte readStatus() {
        return buffer.readByte();
    }

    private int writeStatusEmpty() {
        int index = buffer.position();
        buffer.writeByte(STATUS_EMPTY);
        return index;
    }

    private void setStatusExists(int index) {
        buffer.setByte(index, STATUS_EXISTS);
    }

    private void writeStatusEndOfStream() {
        buffer.writeByte(STATUS_END_OF_STREAM);
    }


    public interface EncodingFactory<T> {
        MessageEncoding<T> create(IpcBuffer buffer);
    }
}
