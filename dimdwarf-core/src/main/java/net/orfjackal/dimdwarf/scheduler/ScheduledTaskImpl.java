/*
 * This file is part of Dimdwarf Application Server <http://dimdwarf.sourceforge.net/>
 *
 * Copyright (c) 2008-2009, Esko Luontola. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
