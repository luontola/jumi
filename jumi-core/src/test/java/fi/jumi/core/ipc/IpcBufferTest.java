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

    private final IpcBuffer buffer = new IpcBuffer();


    // absolute get/set

    @Test
    public void absolute_byte() {
        testAbsolute(1,
                (index) -> buffer.setByte(index, random.nextByte()),
                (index) -> assertThat(buffer.getByte(index), is(random.nextByte()))
        );
    }

    @Test
    public void absolute_short() {
        testAbsolute(2,
                (index) -> buffer.setShort(index, random.nextShort()),
                (index) -> assertThat(buffer.getShort(index), is(random.nextShort()))
        );
    }

    @Test
    public void absolute_char() {
        testAbsolute(2,
                (index) -> buffer.setChar(index, random.nextChar()),
                (index) -> assertThat(buffer.getChar(index), is(random.nextChar()))
        );
    }

    @Test
    public void absolute_int() {
        testAbsolute(4,
                (index) -> buffer.setInt(index, random.nextInt()),
                (index) -> assertThat(buffer.getInt(index), is(random.nextInt()))
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


    // randomized testing

    private void testAbsolute(int valueSize, AbsoluteWriter writer, AbsoluteReader reader) {
        final int startIndex = random.nextInt(10);
        int index;

        random.resetSeed();
        index = startIndex;
        assertReturnedItself(writer.run(index));
        index += valueSize;
        assertReturnedItself(writer.run(index));

        random.resetSeed();
        index = startIndex;
        reader.run(index);
        index += valueSize;
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
