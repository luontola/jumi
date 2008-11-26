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
import net.orfjackal.dimdwarf.api.TaskScheduler;
import net.orfjackal.dimdwarf.tasks.TaskExecutor;
import net.orfjackal.dimdwarf.tx.*;
import net.orfjackal.dimdwarf.util.Clock;
import org.jetbrains.annotations.TestOnly;

import javax.annotation.Nullable;
import javax.annotation.concurrent.*;
import java.util.concurrent.*;

/**
 * @author Esko Luontola
 * @since 24.11.2008
 */
@ThreadSafe
public class TaskSchedulerImpl implements TaskScheduler {

    private static final String TASKS_PREFIX = TaskSchedulerImpl.class.getName() + ".tasks";

    private final BlockingQueue<ScheduledTaskHolder> waitingForExecution = new DelayQueue<ScheduledTaskHolder>();
    private final RecoverableSet<ScheduledTask> savedTasks;
    private final Provider<Transaction> tx;
    private final Clock clock;

    public TaskSchedulerImpl(Provider<Transaction> tx, Clock clock, TaskExecutor taskContext, RecoverableSetFactory rsf) {
        this.tx = tx;
        this.clock = clock;
        savedTasks = rsf.create(TASKS_PREFIX);
        recoverTasksFromDatabase(taskContext);
    }

    private void recoverTasksFromDatabase(TaskExecutor taskContext) {
        taskContext.execute(new Runnable() {
            public void run() {
                for (ScheduledTask st : savedTasks.getAll()) {
                    waitingForExecution.add(new ScheduledTaskHolder(savedTasks.put(st), st.getScheduledTime()));
                }
            }
        });
    }

    public Future<?> submit(Runnable task) {
        return schedule(task, 0, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
        delay = unit.toMillis(delay);
        SchedulingControl control = new SchedulingControl();
        ScheduledTask st = ScheduledOneTimeTask.create(task, delay, control, clock);
        addToExecutionQueue(st);
        return new SchedulingFuture(control);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        initialDelay = unit.toMillis(initialDelay);
        period = unit.toMillis(period);
        SchedulingControl control = new SchedulingControl();
        ScheduledTask st = ScheduledAtFixedRateTask.create(task, initialDelay, period, control, clock);
        addToExecutionQueue(st);
        return new SchedulingFuture(control);
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit unit) {
        initialDelay = unit.toMillis(initialDelay);
        delay = unit.toMillis(delay);
        SchedulingControl control = new SchedulingControl();
        ScheduledTask st = ScheduledWithFixedDelayTask.create(task, initialDelay, delay, control, clock);
        addToExecutionQueue(st);
        return new SchedulingFuture(control);
    }

    private void addToExecutionQueue(ScheduledTask st) {
        enqueueOnCommit(putToSavedTasks(st));
    }

    private ScheduledTaskHolder putToSavedTasks(ScheduledTask st) {
        return new ScheduledTaskHolder(savedTasks.put(st), st.getScheduledTime());
    }

    private void enqueueOnCommit(final ScheduledTaskHolder holder) {
        tx.get().join(new TransactionParticipant() {
            public void prepare() throws Throwable {
            }

            public void commit() {
                waitingForExecution.add(holder);
            }

            public void rollback() {
            }
        });
    }

    public TaskBootstrap takeNextTask() throws InterruptedException {
        return waitingForExecution.take();
    }

    @Nullable
    private Runnable getTaskInsideTransaction0(ScheduledTaskHolder holder) {
        cancelTakeOnRollback(holder);
        ScheduledTask st = removeFromSaveTasks(holder);
        if (st.isDone()) {
            return null;
        }
        repeatIfRepeatable(st);
        return st.getTask();
    }

    private ScheduledTask removeFromSaveTasks(ScheduledTaskHolder holder) {
        return savedTasks.remove(holder.getBinding());
    }

    private void repeatIfRepeatable(ScheduledTask st) {
        ScheduledTask repeat = st.nextRepeatedTask();
        if (repeat != null) {
            addToExecutionQueue(repeat);
        }
    }

    private void cancelTakeOnRollback(final ScheduledTaskHolder holder) {
        tx.get().join(new TransactionParticipant() {
            public void prepare() throws Throwable {
            }

            public void commit() {
            }

            public void rollback() {
                waitingForExecution.add(holder);
            }
        });
    }

    @TestOnly
    int getQueuedTasks() {
        return waitingForExecution.size();
    }


    @Immutable
    private class ScheduledTaskHolder implements Delayed, TaskBootstrap {

        private final String binding;
        private final long scheduledTime;

        public ScheduledTaskHolder(String binding, long scheduledTime) {
            this.binding = binding;
            this.scheduledTime = scheduledTime;
        }

        @Nullable
        public Runnable getTaskInsideTransaction() {
            return getTaskInsideTransaction0(this);
        }

        public String getBinding() {
            return binding;
        }

        public long getDelay(TimeUnit unit) {
            return unit.convert(scheduledTime - clock.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }

        public int compareTo(Delayed o) {
            ScheduledTaskHolder other = (ScheduledTaskHolder) o;
            return (int) (this.scheduledTime - other.scheduledTime);
        }
    }
}
