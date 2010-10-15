// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util;

import org.apache.mina.core.buffer.IoBuffer;
import org.junit.*;
import org.junit.rules.ExpectedException;

import static net.orfjackal.dimdwarf.util.CustomMatchers.*;
import static net.orfjackal.dimdwarf.util.TestUtil.runAsynchronously;

public class AsynchronouslyMatchingBytesFromInputStreamTest {

    public static final long TIMEOUT_NEVER_REACHED = 100;
    public static final long TIMEOUT_IS_REACHED = 1;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void passes_when_expected_bytes_have_already_arrived() {
        ByteSink sink = new ByteSink(TIMEOUT_NEVER_REACHED);

        IoBuffer bytes = IoBuffer.wrap(new byte[]{0x01, 0x02, 0x03});
        sink.append(bytes);

        assertEventually(sink, startsWithBytes(bytes));
    }

    @Test
    public void passes_when_expected_bytes_arrive_asynchronously() {
        final ByteSink sink = new ByteSink(TIMEOUT_NEVER_REACHED);
        final IoBuffer bytes = IoBuffer.wrap(new byte[]{0x01, 0x02, 0x03});

        runAsynchronously(new Runnable() {
            public void run() {
                sink.append(bytes);
            }
        });

        assertEventually(sink, startsWithBytes(bytes));
    }

    @Test
    public void passes_when_expected_bytes_and_some_more_bytes_arrive() {
        ByteSink sink = new ByteSink(TIMEOUT_NEVER_REACHED);

        sink.append(IoBuffer.wrap(new byte[]{0x01, 0x02, 0x03}));
        sink.append(IoBuffer.wrap(new byte[]{0x04, 0x05}));

        assertEventually(sink, startsWithBytes(IoBuffer.wrap(new byte[]{0x01, 0x02, 0x03})));
    }

    @Test
    public void fails_when_different_bytes_have_arrived() {
        ByteSink sink = new ByteSink(TIMEOUT_NEVER_REACHED);

        sink.append(IoBuffer.wrap(new byte[]{0x04, 0x05, 0x06}));

        thrown.expect(AssertionError.class);
        thrown.expectMessage("3 bytes: 01 02 03");  // expected
        thrown.expectMessage("3 bytes: 04 05 06");  // actual
        assertEventually(sink, startsWithBytes(IoBuffer.wrap(new byte[]{0x01, 0x02, 0x03})));
    }

    @Test
    public void fails_due_to_timeout_when_no_bytes_arrive() {
        ByteSink sink = new ByteSink(TIMEOUT_IS_REACHED);

        thrown.expect(AssertionError.class);
        thrown.expectMessage("3 bytes: 01 02 03");  // expected
        thrown.expectMessage("0 bytes:");           // actual
        assertEventually(sink, startsWithBytes(IoBuffer.wrap(new byte[]{0x01, 0x02, 0x03})));
    }

    @Test
    public void fails_due_to_timeout_when_not_enough_bytes_arrive() {
        ByteSink sink = new ByteSink(TIMEOUT_IS_REACHED);

        sink.append(IoBuffer.wrap(new byte[]{0x01, 0x02}));

        thrown.expect(AssertionError.class);
        thrown.expectMessage("3 bytes: 01 02 03");  // expected
        thrown.expectMessage("2 bytes: 01 02");     // actual
        assertEventually(sink, startsWithBytes(IoBuffer.wrap(new byte[]{0x01, 0x02, 0x03})));
    }
}
