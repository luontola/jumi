// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import org.junit.*;
import org.junit.rules.ExpectedException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class IpcBufferTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Rule
    public final TestableRandom random = new TestableRandom();

    private final IpcBuffer buffer = new IpcBuffer(new AllocatedByteBufferSequence(30));


    @Test
    public void buffer_will_increase_capacity_automatically_when_writing_beyond_it() {
        final int initialCapacity = 10;
        final int toBeWritten = initialCapacity + 5;
        IpcBuffer buffer = new IpcBuffer(new AllocatedByteBufferSequence(initialCapacity));

        for (int i = 0; i < toBeWritten; i++) {
            buffer.writeByte(random.nextByte());
        }

        random.resetSeed();
        buffer.position(0);
        for (int i = 0; i < toBeWritten; i++) {
            assertThat("byte at index " + i, buffer.readByte(), is(random.nextByte()));
        }
    }

    /**
     * Opening memory-mapped files is relatively slow.
     */
    @Test
    public void tries_to_minimize_the_number_of_times_that_a_backing_buffer_is_looked_up() {
        AllocatedByteBufferSequence sequence = spy(new AllocatedByteBufferSequence(10));
        IpcBuffer buffer = new IpcBuffer(sequence);

        buffer.setByte(5, (byte) 0);
        buffer.setByte(15, (byte) 0);
        buffer.setByte(5, (byte) 0);
        buffer.setByte(15, (byte) 0);

        verify(sequence, times(1)).get(0);
        verify(sequence, times(1)).get(1);
    }

    @Test
    public void cannot_make_position_negative() {
        thrown.expect(IllegalArgumentException.class);
        buffer.position(-1);
    }

    @Test
    public void cannot_read_from_negative_positions() {
        thrown.expect(IndexOutOfBoundsException.class);
        buffer.getByte(-1);
    }

    @Test
    public void cannot_write_to_negative_positions() {
        thrown.expect(IndexOutOfBoundsException.class);
        buffer.setByte(-1, (byte) 0);
    }

    @Test
    public void test_traversing_forward_and_backward() {
        final int segmentCapacity = 2;
        final int end = segmentCapacity * 5;
        IpcBuffer buffer = new IpcBuffer(new AllocatedByteBufferSequence(segmentCapacity));

        // forward
        for (int i = 0; i < end; i++) {
            buffer.setByte(i, (byte) i);
        }

        // backward
        for (int i = end - 1; i >= 0; i--) {
            assertThat(buffer.getByte(i), is((byte) i));
        }
    }


    // absolute get/set

    @Test
    public void absolute_byte() {
        testAbsolute(Byte.SIZE,
                (index) -> buffer.setByte(index, random.nextByte()),
                (index) -> assertThat(buffer.getByte(index), is(random.nextByte()))
        );
    }

    @Test
    public void absolute_short() {
        testAbsolute(Short.SIZE,
                (index) -> buffer.setShort(index, random.nextShort()),
                (index) -> assertThat(buffer.getShort(index), is(random.nextShort()))
        );
    }

    @Test
    public void absolute_char() {
        testAbsolute(Character.SIZE,
                (index) -> buffer.setChar(index, random.nextChar()),
                (index) -> assertThat(buffer.getChar(index), is(random.nextChar()))
        );
    }

    @Test
    public void absolute_int() {
        testAbsolute(Integer.SIZE,
                (index) -> buffer.setInt(index, random.nextInt()),
                (index) -> assertThat(buffer.getInt(index), is(random.nextInt()))
        );
    }

    @Test
    public void absolute_long() {
        testAbsolute(Long.SIZE,
                (index) -> buffer.setLong(index, random.nextLong()),
                (index) -> assertThat(buffer.getLong(index), is(random.nextLong()))
        );
    }


    // relative read/write

    @Test
    public void relative_byte() {
        testRelative(
                () -> buffer.writeByte(random.nextByte()),
                () -> assertThat(buffer.readByte(), is(random.nextByte()))
        );
    }

    @Test
    public void relative_short() {
        testRelative(
                () -> buffer.writeShort(random.nextShort()),
                () -> assertThat(buffer.readShort(), is(random.nextShort()))
        );
    }

    @Test
    public void relative_char() {
        testRelative(
                () -> buffer.writeChar(random.nextChar()),
                () -> assertThat(buffer.readChar(), is(random.nextChar()))
        );
    }

    @Test
    public void relative_int() {
        testRelative(
                () -> buffer.writeInt(random.nextInt()),
                () -> assertThat(buffer.readInt(), is(random.nextInt()))
        );
    }

    @Test
    public void relative_long() {
        testRelative(
                () -> buffer.writeLong(random.nextLong()),
                () -> assertThat(buffer.readLong(), is(random.nextLong()))
        );
    }


    // randomized testing

    private void testAbsolute(int sizeInBits, AbsoluteWriter writer, AbsoluteReader reader) {
        int sizeInBytes = sizeInBits / Byte.SIZE;
        final int startIndex = random.nextInt(10);
        int index;

        random.resetSeed();
        index = startIndex;
        assertReturnedItself(writer.run(index));
        index += sizeInBytes;
        assertReturnedItself(writer.run(index));

        random.resetSeed();
        index = startIndex;
        reader.run(index);
        index += sizeInBytes;
        reader.run(index);
    }

    private interface AbsoluteWriter {
        IpcBuffer run(int index);
    }

    private interface AbsoluteReader {
        void run(int index);
    }

    private void testRelative(RelativeWriter writer, RelativeReader checker) {
        final int startIndex = random.nextInt(10);

        random.resetSeed();
        buffer.position(startIndex);
        assertReturnedItself(writer.run());
        assertReturnedItself(writer.run());

        random.resetSeed();
        buffer.position(startIndex);
        checker.run();
        checker.run();
    }

    private interface RelativeWriter {
        IpcBuffer run();
    }

    private interface RelativeReader {
        void run();
    }

    private void assertReturnedItself(IpcBuffer run) {
        assertThat("write operations should return `this`", run, is(buffer));
    }
}
