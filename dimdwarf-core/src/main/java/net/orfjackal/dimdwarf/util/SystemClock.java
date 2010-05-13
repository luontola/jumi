// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util;

public class SystemClock implements Clock {

    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }
}
