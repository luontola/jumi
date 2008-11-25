/*
 * This file is part of Dimdwarf Application Server <http://dimdwarf.sourceforge.net/>
 *
 * Copyright (c) 2008, Esko Luontola. All Rights Reserved.
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
 *     * Neither the name of the copyright holder nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
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

import com.google.inject.Inject;
import net.orfjackal.dimdwarf.api.internal.EntityObject;
import net.orfjackal.dimdwarf.util.Clock;

import javax.annotation.CheckForNull;
import javax.annotation.concurrent.ThreadSafe;
import java.io.Serializable;
import java.util.concurrent.*;

/**
 * @author Esko Luontola
 * @since 24.11.2008
 */
@ThreadSafe
public class ScheduledTask implements Delayed, EntityObject, Serializable {
    private static final long serialVersionUID = 1L;

    private static final int NO_FIXED_DELAY = -1;

    private final Runnable task;
    private final long scheduledTime;
    private final long fixedDelay;
    private transient Clock clock;

    public static ScheduledTask newScheduledTask(Runnable task, long initialDelay,
                                                 TimeUnit unit, Clock clock) {
        long scheduledTime = unit.toMillis(initialDelay) + clock.currentTimeMillis();
        long fixedDelay = NO_FIXED_DELAY;
        return new ScheduledTask(task, scheduledTime, fixedDelay, clock);
    }

    public static ScheduledTask newScheduledTaskWithFixedDelay(Runnable task, long initialDelay, long fixedDelay,
                                                               TimeUnit unit, Clock clock) {
        long scheduledTime = unit.toMillis(initialDelay) + clock.currentTimeMillis();
        fixedDelay = unit.toMillis(fixedDelay);
        return new ScheduledTask(task, scheduledTime, fixedDelay, clock);
    }

    private ScheduledTask(Runnable task, long scheduledTime, long fixedDelay, Clock clock) {
        this.task = task;
        this.scheduledTime = scheduledTime;
        this.fixedDelay = fixedDelay;
        this.clock = clock;
    }

    @Inject
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public Runnable getRunnable() {
        return task;
    }

    @CheckForNull
    public ScheduledTask nextRepeatedTask() {
        // TODO: replace with polymorphism: ScheduledOneTimeTask, ScheduledAtFixedRateTask, ScheduledWithFixedDelayTask
        if (repeatWithFixedDelay()) {
            return new ScheduledTask(task, clock.currentTimeMillis() + fixedDelay, fixedDelay, clock);
        }
        return null;
    }

    private boolean repeatWithFixedDelay() {
        return fixedDelay != NO_FIXED_DELAY;
    }

    public long getDelay(TimeUnit unit) {
        return unit.convert(scheduledTime - clock.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    public int compareTo(Delayed o) {
        ScheduledTask other = (ScheduledTask) o;
        return (int) (this.scheduledTime - other.scheduledTime);
    }
}
