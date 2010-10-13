// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util;

public class Timeout {

    private final long endOfTimeout;

    public Timeout(long timeout) {
        endOfTimeout = System.currentTimeMillis() + timeout;
    }

    public boolean hasTimedOut() {
        return millisUntilTimeout() <= 0;
    }

    public void waitUntilTimeout(Object notification) {
        try {
            notification.wait(nonZero(millisUntilTimeout()));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private long millisUntilTimeout() {
        return endOfTimeout - System.currentTimeMillis();
    }

    private static long nonZero(long i) {
        return Math.max(i, 1);
    }
}
