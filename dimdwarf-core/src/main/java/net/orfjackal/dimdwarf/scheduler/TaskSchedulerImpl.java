// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.scheduler;

import com.google.inject.*;
import net.orfjackal.dimdwarf.api.TaskScheduler;
import net.orfjackal.dimdwarf.tasks.Task;
import net.orfjackal.dimdwarf.tx.*;
import net.orfjackal.dimdwarf.util.Clock;
import org.jetbrains.annotations.TestOnly;

import javax.annotation.Nullable;
import javax.annotation.concurrent.*;
import java.util.concurrent.*;

@Singleton
@ThreadSafe
public class TaskSchedulerImpl implements TaskScheduler, TaskProducer {

    // FIXME: TaskSchedulerImpl will be removed/refactored in new architecture

    private static final String TASKS_PREFIX = TaskSchedulerImpl.class.getName() + ".tasks";

    // TODO: Extract 'scheduledTasks' into its own class, for example NonPersistedScheduledTasksQueue
    private final BlockingQueue<ScheduledTaskHolder> scheduledTasks = new DelayQueue<ScheduledTaskHolder>();
    private final RecoverableSet<ScheduledTask> persistedTasks;

    private final Provider<Transaction> tx;
    private final Clock clock;
    private final Executor taskContext;

    @Inject
    public TaskSchedulerImpl(Provider<Transaction> tx,
                             Clock clock,
                             @Task Executor taskContext,
                             RecoverableSetFactory rsf) {
        this.tx = tx;
        this.clock = clock;
        this.taskContext = taskContext;
        this.persistedTasks = rsf.create(TASKS_PREFIX);
    }

    public void start() {
        recoverTasksFromDatabase();
    }

    private void recoverTasksFromDatabase() {
        // TODO: move recovery of scheduled tasks to a new class
        taskContext.execute(new Runnable() {
            public void run() {
                for (ScheduledTask st : persistedTasks.getAll()) {
                    String binding = persistedTasks.put(st);
                    long scheduledTime = st.getScheduledTime();
                    scheduledTasks.add(new ScheduledTaskHolder(binding, scheduledTime));
                }
            }
        });
    }

    public Future<?> submit(Runnable task) {
        return schedule(task, 0, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
        delay = unit.toMillis(delay);
        ScheduledTask st = new ScheduledTaskImpl(task, ScheduledOneTimeRun.create(delay, clock));
        enqueueDurableTask(st);
        return new SchedulingFuture(st);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        initialDelay = unit.toMillis(initialDelay);
        period = unit.toMillis(period);
        ScheduledTask st = new ScheduledTaskImpl(task, ScheduledAtFixedRate.create(initialDelay, period, clock));
        enqueueDurableTask(st);
        return new SchedulingFuture(st);
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit unit) {
        initialDelay = unit.toMillis(initialDelay);
        delay = unit.toMillis(delay);
        ScheduledTask st = new ScheduledTaskImpl(task, ScheduledWithFixedDelay.create(initialDelay, delay, clock));
        enqueueDurableTask(st);
        return new SchedulingFuture(st);
    }

    private void enqueueDurableTask(ScheduledTask st) {
        ScheduledTaskHolder h = saveToDatabase(st);
        enqueueOnCommit(h);
    }

    private ScheduledTaskHolder saveToDatabase(ScheduledTask st) {
        String binding = persistedTasks.put(st);
        long scheduledTime = st.getScheduledTime();
        return new ScheduledTaskHolder(binding, scheduledTime);
    }

    private void enqueueOnCommit(final ScheduledTaskHolder holder) {
        tx.get().join(new TransactionParticipant() {
            public void prepare() throws Throwable {
            }

            public void commit() {
                scheduledTasks.add(holder);
            }

            public void rollback() {
            }
        });
    }

    public TaskBootstrap takeNextTask() throws InterruptedException {
        return scheduledTasks.take();
    }

    public TaskBootstrap pollNextTask() {
        return scheduledTasks.poll();
    }

    @Nullable
    private Runnable getTaskInsideTransaction0(ScheduledTaskHolder holder) {
        cancelTakeOnRollback(holder);
        ScheduledTask task = takeFromDatabase(holder);
        if (task.isDone()) {
            return null;
        }
        Runnable run = task.startScheduledRun();
        if (!task.isDone()) {
            enqueueDurableTask(task);
        }
        return run;
    }

    private ScheduledTask takeFromDatabase(ScheduledTaskHolder holder) {
        return persistedTasks.remove(holder.getBinding());
    }

    private void cancelTakeOnRollback(final ScheduledTaskHolder holder) {
        // TODO: remove the retry code from this class, because SingleThreadFallbackTaskExecutor and RetryingTaskExecutor will take care of retrying
        // FIXME: If the task fails and the retry limit is reached, the task should be removed from the database
        // or cancelled, so that it will not be rescheduled when the system is restarted. 
        tx.get().join(new TransactionParticipant() {
            public void prepare() throws Throwable {
            }

            public void commit() {
            }

            public void rollback() {
                scheduledTasks.add(holder);
            }
        });
    }

    @TestOnly
    int getQueuedTasks() {
        return scheduledTasks.size();
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
