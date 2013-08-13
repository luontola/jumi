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

    @Test
    public void absolute_byte() {
        byte b1 = random.nextByte();
        byte b2 = random.nextByte();

        IpcBuffer buffer = new IpcBuffer()
                .setByte(0, b1)
                .setByte(1, b2);

        assertThat(buffer.getByte(0), is(b1));
        assertThat(buffer.getByte(1), is(b2));
    }

    @Test
    public void relative_byte() {
        byte b1 = random.nextByte();
        byte b2 = random.nextByte();

        IpcBuffer buffer = new IpcBuffer()
                .writeByte(b1)
                .writeByte(b2)
                .position(0);

        assertThat(buffer.readByte(), is(b1));
        assertThat(buffer.readByte(), is(b2));
    }

    // TODO: generic scaffolding for random data; on failure show the seed that was used
}
