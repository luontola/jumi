// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util;

import org.hamcrest.SelfDescribing;

public abstract class AsynchronousSink<T> implements SelfDescribing {

    private final long timeout;

    public AsynchronousSink(long timeout) {
        this.timeout = timeout;
    }

    public long getTimeout() {
        return timeout;
    }

    public abstract void append(T event);

    public abstract T getContent();
}
