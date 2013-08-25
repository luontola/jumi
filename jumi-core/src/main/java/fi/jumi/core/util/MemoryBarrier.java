// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.util;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.atomic.AtomicBoolean;

@ThreadSafe
public class MemoryBarrier {

    private final AtomicBoolean var = new AtomicBoolean();

    public void storeStore() {
        var.lazySet(true);
    }

    public void storeLoad() {
        var.set(true);
    }

    public void read() {
        var.get();
    }
}
