// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

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
