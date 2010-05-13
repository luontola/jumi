// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util;

public class DummyClock implements Clock {

    private volatile long currentTimeMillis = 0;

    public void addTime(long millis) {
        currentTimeMillis += millis;
    }

    public long currentTimeMillis() {
        return currentTimeMillis;
    }
}
