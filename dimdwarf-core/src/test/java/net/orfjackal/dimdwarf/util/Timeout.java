// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util;

public class Timeout {
    private final long endOfTimeout;

    public Timeout(long timeout) {
        endOfTimeout = System.currentTimeMillis() + timeout;
    }

    public boolean hasNotTimedOut() {
        return System.currentTimeMillis() <= endOfTimeout;
    }

    public void waitUntilTimeout(Object lock) throws InterruptedException {
        long untilEnd = endOfTimeout - System.currentTimeMillis();
        lock.wait(nonZero(untilEnd));
    }

    private static long nonZero(long i) {
        return Math.max(i, 1);
    }
}
