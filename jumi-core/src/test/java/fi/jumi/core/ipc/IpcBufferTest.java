// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class IpcBufferTest {

    @Test
    public void absolute_byte() {
        IpcBuffer buffer = new IpcBuffer()
                .setByte(0, (byte) 40)
                .setByte(1, (byte) 50);

        assertThat(buffer.getByte(0), is((byte) 40));
        assertThat(buffer.getByte(1), is((byte) 50));
    }

    @Test
    public void relative_byte() {
        IpcBuffer buffer = new IpcBuffer()
                .writeByte((byte) 40)
                .writeByte((byte) 50)
                .position(0);

        assertThat(buffer.readByte(), is((byte) 40));
        assertThat(buffer.readByte(), is((byte) 50));
    }

    // TODO: generic scaffolding for random data; on failure show the seed that was used
}
