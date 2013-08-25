// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.util;

import java.util.*;
import java.util.concurrent.*;

public class ConcurrencyUtil {

    public static void runConcurrently(Runnable... tasks) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(tasks.length);
        CountDownLatch allTasksStarted = new CountDownLatch(tasks.length);
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (Runnable task : tasks) {
                futures.add(executor.submit(() -> {
                    sync(allTasksStarted, 1000);
                    task.run();
                }));
            }
            for (Future<?> future : futures) {
                future.get();
            }
        } finally {
            executor.shutdownNow();
        }
    }

    public static Thread startThread(Runnable task) {
        Thread t = new Thread(task);
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
