// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc.channel;

import fi.jumi.actors.eventizers.Event;
import fi.jumi.core.ipc.buffer.IpcBuffer;
import fi.jumi.core.ipc.encoding.MessageEncoding;
import fi.jumi.core.util.MemoryBarrier;

import javax.annotation.concurrent.NotThreadSafe;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static fi.jumi.core.ipc.encoding.StringEncoding.*;

@NotThreadSafe
public class IpcProtocol<T> implements IpcReader<T>, IpcWriter<T> {

    private static final byte[] HEADER_MAGIC_BYTES = "Jumi".getBytes(StandardCharsets.US_ASCII);
    private static final int PROTOCOL_VERSION = 1;

    private static final byte STATUS_EMPTY = 0;
    private static final byte STATUS_EXISTS = 1;
    private static final byte STATUS_END_OF_STREAM = 2;

    private final MemoryBarrier memoryBarrier = new MemoryBarrier();
    private final IpcBuffer buffer;
    private final MessageEncoding<T> messageEncoding;

    public IpcProtocol(IpcBuffer buffer, EncodingFactory<T> encodingFactory) {
        this.buffer = buffer;
        this.messageEncoding = encodingFactory.create(buffer);
    }


    // write operations

    public void start() {
        writeHeader();
    }

    @Override
    public void send(Event<T> message) {
        int currentMessage = writeStatusEmpty();
        messageEncoding.encode(message);
        initNextMessage();

        memoryBarrier.storeStore();
        setStatusExists(currentMessage);
    }

    @Override
    public void close() {
        writeStatusEndOfStream();
    }


    // read operations

    @Override
    public PollResult poll(T target) {
        int index = buffer.position();

        byte status = readStatus();
        if (status == STATUS_EMPTY) {
            buffer.position(index);
            return PollResult.NO_NEW_MESSAGES;
        }
        if (status == STATUS_END_OF_STREAM) {
            return PollResult.END_OF_STREAM;
        }
        memoryBarrier.loadLoad();

        if (index == 0) {
            // For the header, the first byte works both as the status (when zero),
            // and part of the magic bytes (when non-zero), so we must not consume the byte before readHeader().
            buffer.position(index);
            readHeader();

        } else {
            assert status == STATUS_EXISTS : "unexpected status: " + status;
            messageEncoding.decode(target);
        }
        return PollResult.HAD_SOME_MESSAGES;
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
        checkMagicBytes();
        checkProtocolVersion();
        checkInterface();
        checkInterfaceVersion();
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

    private void initNextMessage() {
        // Write empty status for next message, so that the producer
        // is the first to touch a new segment, thus determining its size.
        buffer.setByte(buffer.position(), STATUS_EMPTY);
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
