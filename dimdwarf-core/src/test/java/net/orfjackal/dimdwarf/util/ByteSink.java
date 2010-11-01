// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util;

import org.apache.mina.core.buffer.IoBuffer;
import org.hamcrest.*;

public class ByteSink extends AsynchronousSink<IoBuffer> implements SelfDescribing {

    private final IoBuffer sink = IoBuffer.allocate(100).setAutoExpand(true);

    public ByteSink(long timeout) {
        super(timeout);
    }

    public synchronized void append(IoBuffer event) {
        sink.put(event.duplicate());
        notifyAll();
    }

    public synchronized IoBuffer getContent() {
        return sink.duplicate().flip();
    }

    public void describeTo(Description description) {
        IoBuffer buf = getContent();
        description
                .appendText("had " + buf.remaining() + " bytes: ")
                .appendText(buf.getHexDump());
    }
}
