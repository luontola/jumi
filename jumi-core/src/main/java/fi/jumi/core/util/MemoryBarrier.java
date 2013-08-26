// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.util;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Operations for creating memory barriers. More information:
 * <ul>
 *     <li><a href="http://g.oswego.edu/dl/jmm/cookbook.html">The JSR-133 Cookbook for Compiler Writers</a></li>
 *     <li><a href="http://psy-lob-saw.blogspot.com/2012/12/atomiclazyset-is-performance-win-for.html">Atomic*.lazySet is a performance win for single writers</a></li>
 *     <li><a href="http://brooker.co.za/blog/2012/09/10/volatile.html">Are volatile reads really free?</a></li>
 * </ul>
 */
@ThreadSafe
public class MemoryBarrier {

    private final AtomicBoolean var = new AtomicBoolean();

    public void storeStore() {
        var.lazySet(true);
    }

    public void storeLoad() {
        var.set(true);
    }

    public void loadLoad() {
        var.get();
    }
}
