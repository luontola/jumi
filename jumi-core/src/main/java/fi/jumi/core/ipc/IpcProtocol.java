// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.queue.MessageSender;
import fi.jumi.core.ipc.buffer.IpcBuffer;
import fi.jumi.core.util.MemoryBarrier;

import javax.annotation.concurrent.NotThreadSafe;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static fi.jumi.core.ipc.StringEncoding.*;

@NotThreadSafe
public class IpcProtocol<T> implements MessageSender<Event<T>> {

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

    public DecodeResult decodeNextMessage(T target) {
        int index = buffer.position();
        byte status = readStatus();
        // TODO: we should do a read barrier here

        if (status == STATUS_EMPTY) {
            buffer.position(index);
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return DecodeResult.NO_NEW_MESSAGES;

        } else if (status == STATUS_END_OF_STREAM) {
            return DecodeResult.FINISHED;

        } else {
            if (index == 0) {
                // For the header, the first byte works both as the status (when zero),
                // and part of the magic bytes (when non-zero), so we must not consume the byte before readHeader().
                buffer.position(index);
                readHeader();

            } else {
                assert status == STATUS_EXISTS : "unexpected status: " + status;
                messageEncoding.decode(target);
            }
            return DecodeResult.GOT_MESSAGE;
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
