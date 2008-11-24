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

import com.google.inject.Provider;
import net.orfjackal.dimdwarf.api.EntityInfo;
import net.orfjackal.dimdwarf.api.internal.EntityObject;
import net.orfjackal.dimdwarf.entities.*;
import net.orfjackal.dimdwarf.tasks.TaskExecutor;
import org.jetbrains.annotations.TestOnly;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.concurrent.*;

/**
 * @author Esko Luontola
 * @since 24.11.2008
 */
public class TaskScheduler extends NullScheduledExecutorService {

    private static final String TASKS_PREFIX = TaskScheduler.class.getName() + ".tasks.";

    private final BlockingQueue<ScheduledTask> waitingForExecution = new DelayQueue<ScheduledTask>();
    private final Provider<BindingStorage> bindings;
    private final Provider<EntityInfo> entities;

    public TaskScheduler(Provider<BindingStorage> bindings, Provider<EntityInfo> entities, TaskExecutor taskContext) {
        this.bindings = bindings;
        this.entities = entities;
        recoverTasksFromDatabase(taskContext);
    }

    private void recoverTasksFromDatabase(TaskExecutor taskContext) {
        taskContext.execute(new Runnable() {
            public void run() {
                for (String binding : new BindingWalker(TASKS_PREFIX, bindings.get())) {
                    recoverTaskFromDatabase(binding);
                }
            }
        });
    }

    private void recoverTaskFromDatabase(String binding) {
        ScheduledTask task = (ScheduledTask) bindings.get().read(binding);
        if (task != null) {
            waitingForExecution.add(task);
        }
    }

    public Future<?> submit(Runnable task) {
        return schedule(task, 0, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
        ScheduledTask st = new ScheduledTask(task, delay, unit);
        bindings.get().update(bindingFor(st), st);
        waitingForExecution.add(st);
        return null;
    }

    public Runnable takeNextTask() throws InterruptedException {
        ScheduledTask st = waitingForExecution.take();
        bindings.get().delete(bindingFor(st));
        return st.getRunnable();
    }

    private String bindingFor(ScheduledTask task) {
        BigInteger entityId = entities.get().getEntityId(task);
        return TASKS_PREFIX + entityId;
    }

    @TestOnly
    int getQueuedTasks() {
        return waitingForExecution.size();
    }

    private static class ScheduledTask implements Delayed, EntityObject, Serializable {
        private static final long serialVersionUID = 1L;

        private final Runnable task;
        private final long targetTime;

        public ScheduledTask(Runnable task, long delay, TimeUnit unit) {
            this.task = task;
            this.targetTime = unit.toMillis(delay) + System.currentTimeMillis();
        }

        public Runnable getRunnable() {
            return task;
        }

        public long getDelay(TimeUnit unit) {
            return targetTime - System.currentTimeMillis();
        }

        public int compareTo(Delayed o) {
            return 0;
        }
    }
}
