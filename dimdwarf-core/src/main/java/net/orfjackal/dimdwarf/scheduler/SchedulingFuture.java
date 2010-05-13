// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.scheduler;

import javax.annotation.concurrent.Immutable;
import java.io.Serializable;
import java.util.concurrent.*;

@Immutable
public class SchedulingFuture implements ScheduledFuture<Object>, Serializable {
    private static final long serialVersionUID = 1L;

    private final ScheduledTask task;

    public SchedulingFuture(ScheduledTask task) {
        this.task = task;
    }

    public long getDelay(TimeUnit unit) {
        return task.getDelay(unit);
    }

    public int compareTo(Delayed other) {
        long myDelay = getDelay(TimeUnit.MILLISECONDS);
        long otherDelay = other.getDelay(TimeUnit.MILLISECONDS);
        return (int) (myDelay - otherDelay);
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        task.setCancelled();
        return true;
    }

    public boolean isCancelled() {
        return task.isCancelled();
    }

    public boolean isDone() {
        return task.isDone();
    }

    public Object get() throws InterruptedException, ExecutionException {
        throw new UnsupportedOperationException();
    }

    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        throw new UnsupportedOperationException();
    }
}
