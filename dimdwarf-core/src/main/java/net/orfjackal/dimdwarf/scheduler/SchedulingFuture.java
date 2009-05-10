// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.scheduler;

import javax.annotation.concurrent.Immutable;
import java.io.Serializable;
import java.util.concurrent.*;

/**
 * @author Esko Luontola
 * @since 25.11.2008
 */
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
