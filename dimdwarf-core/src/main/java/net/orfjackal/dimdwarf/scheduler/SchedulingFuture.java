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
