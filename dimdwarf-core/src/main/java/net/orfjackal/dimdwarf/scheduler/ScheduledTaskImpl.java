// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.scheduler;

import net.orfjackal.dimdwarf.api.Entity;
import net.orfjackal.dimdwarf.api.internal.EntityObject;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * @author Esko Luontola
 * @since 25.11.2008
 */
@Entity
public class ScheduledTaskImpl implements EntityObject, Serializable, ScheduledTask {
    private static final long serialVersionUID = 1L;

    // TODO: For all references to this class, do not rely on transparent references, but use entity references directly.
    // Otherwise relying on transparent references may complicate Dimdwarf's internals too much, maybe even create
    // non-explicit cyclic dependencies (code expects tref support from the container), which may complicate testing.

    private final Runnable task;
    @Nullable private SchedulingStrategy nextRun;
    private boolean cancelled = false;

    public ScheduledTaskImpl(Runnable task, SchedulingStrategy nextRun) {
        this.task = task;
        this.nextRun = nextRun;
    }

    public Runnable startScheduledRun() {
        assert !isCancelled();
        assert !isDone();
        nextRun = nextRun.nextRepeatedRun();
        return task;
    }

    public long getScheduledTime() {
        return nextRun.getScheduledTime();
    }

    public long getDelay(TimeUnit unit) {
        return nextRun.getDelay(unit);
    }

    public boolean isDone() {
        return nextRun == null || isCancelled();
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled() {
        cancelled = true;
    }
}
