// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util;

import org.apache.mina.core.buffer.IoBuffer;
import org.hamcrest.*;

public class ByteSink extends AbstractSink<IoBuffer> implements SelfDescribing {

    private final IoBuffer sink = IoBuffer.allocate(100).setAutoExpand(true);

    public ByteSink(long timeout) {
        super(timeout);
    }

    protected void doAppend(IoBuffer bytes) {
        sink.put(bytes.duplicate());
    }

    protected boolean doMatch(Matcher<?> matcher) {
        IoBuffer actual = sink.duplicate().flip();
        return matcher.matches(actual);
    }

    public String toString() {
        return sink.toString();
    }

    public void describeTo(Description description) {
        IoBuffer actual = sink.duplicate().flip();
        description
                .appendText(actual.remaining() + " bytes: ")
                .appendText(actual.getHexDump());
    }
}
