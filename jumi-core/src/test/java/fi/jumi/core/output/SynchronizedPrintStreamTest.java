// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.output;

import org.apache.commons.io.output.NullOutputStream;
import org.junit.Test;

import java.io.*;
import java.nio.charset.Charset;
import java.util.concurrent.locks.ReentrantLock;

import static fi.jumi.core.util.ConcurrencyUtil.runConcurrently;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SynchronizedPrintStreamTest {

    private static final long TIMEOUT = 1000;

    private final ReentrantLock lock = new ReentrantLock();

    @Test
    public void synchronizes_all_methods_on_the_lock_given_as_parameter() {
        SpyOutputStream spy = new SpyOutputStream();
        PrintStream printStream = SynchronizedPrintStream.create(spy, Charset.defaultCharset(), lock);

        printStream.println("foo");

        assertThat("was called", spy.wasCalled, is(true));
        assertThat("used the lock", spy.lockWasHeldByCurrentThread, is(true));
    }

    /**
     * For example {@link Throwable#printStackTrace} does this, we must be careful to always acquire a lock on the
     * monitor of the PrintStream first, before all other locks.
     */
    @Test(timeout = TIMEOUT)
    public void does_not_deadlock_if_somebody_locks_in_the_PrintStream_externally() throws InterruptedException {
        final int ITERATIONS = 10;
        final PrintStream printStream = SynchronizedPrintStream.create(new NullOutputStream(), Charset.defaultCharset(), lock);

        // will fail with a test timeout if a deadlock happens
        runConcurrently(
                new Runnable() {
                    @Override
                    public void run() {
                        // what Thread.printStackTrace() would do
                        synchronized (printStream) {
                            for (int i = 0; i < ITERATIONS; i++) {
                                Thread.yield();
                                printStream.print("X");
                            }
                        }
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        // what a normal printer would do
                        for (int i = 0; i < ITERATIONS; i++) {
                            Thread.yield();
                            printStream.print("X");
                        }
                    }
                }
        );
    }


    private class SpyOutputStream extends OutputStream {
        boolean wasCalled = false;
        boolean lockWasHeldByCurrentThread = false;

        @Override
        public void write(int b) {
            wasCalled = true;
            lockWasHeldByCurrentThread = lock.isHeldByCurrentThread();
        }
    }
}
