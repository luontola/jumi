// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.api;

import java.util.concurrent.*;

public interface TaskScheduler {

    // TODO: replace Future and ScheduledFuture with a custom interface, which does not have unnecessary operations (e.g. get)
    // Or is it even necessary to return anything? Why not make the task itself responsible for deciding whether it should run?

    /**
     * @see ExecutorService#submit(Runnable)
     */
    Future<?> submit(Runnable task);

    /**
     * @see ScheduledExecutorService#schedule(Runnable, long, TimeUnit)
     */
    ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit);

    // TODO: are scheduleAtFixedRate and scheduleWithFixedDelay needed? should gather more use cases and simplify the API

    /**
     * @see ScheduledExecutorService#scheduleAtFixedRate
     */
    ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit);

    /**
     * @see ScheduledExecutorService#scheduleWithFixedDelay
     */
    ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit unit);
}
