// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util;

/**
 * @author Esko Luontola
 * @since 13.12.2008
 */
public abstract class ThrowingRunnable implements Runnable {

    public final void run() {
        try {
            doRun();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public abstract void doRun() throws Throwable;
}
