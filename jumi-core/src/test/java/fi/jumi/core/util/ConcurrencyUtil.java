// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.util;

import java.util.*;
import java.util.concurrent.*;

public class ConcurrencyUtil {

    public static void runConcurrently(Runnable... commands) throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        for (Runnable command : commands) {
            threads.add(startThread(command));
        }
        for (Thread thread : threads) {
            thread.join();
        }
    }

    public static Thread startThread(Runnable command) {
        Thread t = new Thread(command);
        t.start();
        return t;
    }

    public static void sync(CountDownLatch barrier, long timeoutMillis) {
        barrier.countDown();
        await(barrier, timeoutMillis);
    }

    public static void await(CountDownLatch barrier, long timeoutMillis) {
        try {
            barrier.await(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
