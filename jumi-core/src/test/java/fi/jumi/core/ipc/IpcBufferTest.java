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

    @Test
    public void absolute_byte() {
        testAbsolute(
                (index) -> buffer.setByte(index, random.nextByte()),
                (index) -> assertThat(buffer.getByte(index), is(random.nextByte()))
        );
    }

    @Test
    public void relative_byte() {
        testRelative(
                () -> buffer.writeByte(random.nextByte()),
                () -> assertThat(buffer.readByte(), is(random.nextByte()))
        );
    }


    // randomized testing

    private void testAbsolute(AbsoluteWriter writer, AbsoluteReader reader) {
        writer.run(0);
        writer.run(1);
        random.resetSeed();
        reader.run(0);
        reader.run(1);
    }

    private interface AbsoluteWriter {
        void run(int index);
    }

    private interface AbsoluteReader {
        void run(int index);
    }

    private void testRelative(RelativeWriter writer, RelativeReader checker) {
        writer.run();
        writer.run();
        buffer.position(0);
        random.resetSeed();
        checker.run();
        checker.run();
    }

    private interface RelativeWriter {
        void run();
    }

    private interface RelativeReader {
        void run();
    }
}
