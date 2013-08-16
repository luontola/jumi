// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import org.junit.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class IpcBufferTest {

    @Rule
    public final TestableRandom random = new TestableRandom();

    private final IpcBuffer buffer = new IpcBuffer(30);


    @Ignore // TODO
    @Test
    public void buffer_will_increase_capacity_automatically_when_writing_beyond_it() {
        final int initialCapacity = 10;
        final int overCapacity = 15;
        final int increasedCapacity = 20;

        IpcBuffer buffer = new IpcBuffer(initialCapacity);
        assertThat("capacity before", buffer.capacity(), is(initialCapacity));

        for (int i = 0; i < overCapacity; i++) {
            buffer.writeByte(random.nextByte());
        }

        assertThat("capacity after", buffer.capacity(), is(increasedCapacity));
        buffer.position(0);
        random.resetSeed();
        for (int i = 0; i < overCapacity; i++) {
            assertThat("index " + i, buffer.readByte(), is(random.nextByte()));
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
        assertReturnedItself(writer.run());
        assertReturnedItself(writer.run());
        buffer.position(0);
        random.resetSeed();
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
