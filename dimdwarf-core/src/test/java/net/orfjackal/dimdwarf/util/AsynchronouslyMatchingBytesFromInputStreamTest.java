// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util;

import org.apache.mina.core.buffer.IoBuffer;
import org.junit.Test;

import static net.orfjackal.dimdwarf.util.CustomMatchers.*;
import static net.orfjackal.dimdwarf.util.TestUtil.runAsynchronously;

public class AsynchronouslyMatchingBytesFromInputStreamTest {

    public static final long TIMEOUT_NOT_REACHED = 100;
    public static final long TIMEOUT_IS_REACHED = 1;

    @Test
    public void passes_when_expected_bytes_have_already_arrived() {
        ByteSink sink = new ByteSink(TIMEOUT_NOT_REACHED);

        IoBuffer bytes = IoBuffer.wrap(new byte[]{0x01, 0x02, 0x03});
        sink.append(bytes);

        assertEventually(sink, startsWithBytes(bytes));
    }

    @Test
    public void passes_when_expected_bytes_arrive_asynchronously() {
        final ByteSink sink = new ByteSink(TIMEOUT_NOT_REACHED);
        final IoBuffer bytes = IoBuffer.wrap(new byte[]{0x01, 0x02, 0x03});

        runAsynchronously(new Runnable() {
            public void run() {
                sink.append(bytes);
            }
        });

        assertEventually(sink, startsWithBytes(bytes));
    }

    @Test(expected = AssertionError.class)
    public void fails_when_different_bytes_have_arrived() {
        ByteSink sink = new ByteSink(TIMEOUT_NOT_REACHED);

        sink.append(IoBuffer.wrap(new byte[]{0x04, 0x05, 0x06}));

        assertEventually(sink, startsWithBytes(IoBuffer.wrap(new byte[]{0x01, 0x02, 0x03})));
    }

    @Test(expected = AssertionError.class)
    public void fails_due_to_timeout_when_no_bytes_arrive() {
        ByteSink sink = new ByteSink(TIMEOUT_IS_REACHED);

        assertEventually(sink, startsWithBytes(IoBuffer.wrap(new byte[]{0x01, 0x02, 0x03})));
    }

    @Test(expected = AssertionError.class)
    public void fails_due_to_timeout_when_not_enough_bytes_arrive() {
        ByteSink sink = new ByteSink(TIMEOUT_IS_REACHED);

        sink.append(IoBuffer.wrap(new byte[]{0x01, 0x02}));

        assertEventually(sink, startsWithBytes(IoBuffer.wrap(new byte[]{0x01, 0x02, 0x03})));
    }

    // TODO: exception messages, must show which bytes have arrived
}
