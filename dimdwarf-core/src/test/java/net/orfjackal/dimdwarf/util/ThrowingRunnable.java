// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util;

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
