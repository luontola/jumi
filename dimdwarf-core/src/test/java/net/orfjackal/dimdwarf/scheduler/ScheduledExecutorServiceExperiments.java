// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.scheduler;

import java.util.concurrent.*;

public class ScheduledExecutorServiceExperiments {

    public static void main(String[] args) throws Exception {
        ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(new Runnable() {
            public int i;

            public void run() {
                System.out.println(i);
                i++;
            }
        }, 0, 1, TimeUnit.SECONDS);

        Thread.sleep(3100);

        System.out.println("future.isDone() = " + future.isDone()); // false
        System.out.println("future.isCancelled() = " + future.isCancelled()); // false
        System.out.println("future.getDelay(TimeUnit.MILLISECONDS) = "
                + future.getDelay(TimeUnit.MILLISECONDS)); // milliseconds until the next execution

        //System.out.println("future.get() = " + future.get()); // blocks indefinitely
        future.cancel(false); // cancels all the repeats

        System.out.println("\nafter cancel:");
        System.out.println("future.isDone() = " + future.isDone()); // true
        System.out.println("future.isCancelled() = " + future.isCancelled()); // true
        System.out.println("future.getDelay(TimeUnit.MILLISECONDS) = "
                + future.getDelay(TimeUnit.MILLISECONDS)); // the same as above

        Thread.sleep(3000);

        System.out.println("\nafter wait:");
        System.out.println("future.isDone() = " + future.isDone()); // true
        System.out.println("future.isCancelled() = " + future.isCancelled()); // true
        System.out.println("future.getDelay(TimeUnit.MILLISECONDS) = "
                + future.getDelay(TimeUnit.MILLISECONDS)); // negative - the time when the next execution would have been 

        scheduler.shutdown();
    }
}
