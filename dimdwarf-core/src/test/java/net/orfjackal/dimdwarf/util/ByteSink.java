// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util;

import org.apache.mina.core.buffer.IoBuffer;

public class ByteSink {

    private final IoBuffer sink = IoBuffer.allocate(100).setAutoExpand(true);
    private final long timeout;

    public ByteSink(long timeout) {
        this.timeout = timeout;
    }

    public void append(IoBuffer bytes) {
        synchronized (sink) {
            sink.put(bytes.duplicate());
            sink.notifyAll();
        }
    }

    public boolean startsWithBytes(IoBuffer expected) {
        Timeout timeout = new Timeout(this.timeout);

        synchronized (sink) {
            waitForDataOrTimeout(expected, timeout);
            IoBuffer actual = sink.duplicate().flip();
            return actual.equals(expected);
        }
    }

    private void waitForDataOrTimeout(IoBuffer expected, Timeout timeout) {
        try {
            while (sink.position() < expected.limit() && timeout.hasNotTimedOut()) {
                timeout.waitUntilTimeout(sink);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public String toString() {
        return sink.toString();
    }

}
