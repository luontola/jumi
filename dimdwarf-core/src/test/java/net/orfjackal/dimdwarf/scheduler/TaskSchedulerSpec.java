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

import com.google.inject.*;
import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.api.EntityInfo;
import net.orfjackal.dimdwarf.context.ThreadContext;
import net.orfjackal.dimdwarf.entities.*;
import net.orfjackal.dimdwarf.modules.*;
import net.orfjackal.dimdwarf.tasks.TaskExecutor;
import net.orfjackal.dimdwarf.tx.Transaction;
import net.orfjackal.dimdwarf.util.*;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Esko Luontola
 * @since 24.11.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class TaskSchedulerSpec extends Specification<Object> {

    private TaskSchedulerImpl scheduler;
    private Provider<BindingStorage> bindings;
    private Provider<EntityInfo> info;
    private Provider<Transaction> tx;
    private TaskExecutor taskContext;
    private RecoverableSetFactory rsf;
    private DummyClock clock;

    private DummyTask task1;
    private DummyTask task2;

    public void create() {
        clearInterruptedStatus();

        clock = new DummyClock();
        Injector injector = Guice.createInjector(
                new EntityModule(),
                new DatabaseModule(),
                new TaskContextModule(),
                new AbstractModule() {
                    protected void configure() {
                        bind(Clock.class).toInstance(clock);
                    }
                });
        bindings = injector.getProvider(BindingStorage.class);
        info = injector.getProvider(EntityInfo.class);
        tx = injector.getProvider(Transaction.class);
        taskContext = injector.getInstance(TaskExecutor.class);
        rsf = new RecoverableSetFactory() {
            public <T> RecoverableSet<T> create(String prefix) {
                return new RecoverableSetImpl<T>(prefix, bindings, info);
            }
        };
        specify(thereMayBeBindingsInOtherNamespaces());

        scheduler = new TaskSchedulerImpl(tx, clock, taskContext, rsf);
        scheduler.start();
        task1 = new DummyTask("1");
        task2 = new DummyTask("2");
    }

    public void destroy() throws Exception {
        clearInterruptedStatus();
    }

    private static void clearInterruptedStatus() {
        // clear interrupted status, in case one of the tests which use interrupts failed
        Thread.interrupted();
    }

    private boolean thereMayBeBindingsInOtherNamespaces() {
        taskContext.execute(new Runnable() {
            public void run() {
                bindings.get().update("a.shouldNotTouchThis", new DummyEntity());
                bindings.get().update("z.shouldNotTouchThis", new DummyEntity());
            }
        });
        return true;
    }

    // Utility methods which should be executed outside a task context.
    // They will create their own task context when necessary.

    private boolean taskMayBeTakenRightNow() {
        final TaskBootstrap bootstrap = takeNextTaskFrom(scheduler);
        taskContext.execute(new Runnable() {
            public void run() {
                specify(bootstrap.getTaskInsideTransaction(), should.not().equal(null));
            }
        });
        return true;
    }

    public static TaskBootstrap takeNextTaskFrom(TaskSchedulerImpl scheduler) {
        assert ThreadContext.getCurrentContext() == null;
        try {
            return scheduler.takeNextTask();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean thereAreNoExecutableTasksRightNow() {
        while (true) {
            TaskBootstrap bootstrap = scheduler.pollNextTask();
            if (bootstrap == null) {
                return true; // no expired tasks in queue
            } else {
                specify(isNullTask(bootstrap)); // current task is cancelled, but there might still be more
            }
        }
    }

    private boolean isNullTask(final TaskBootstrap bootstrap) {
        final AtomicBoolean isNull = new AtomicBoolean();
        taskContext.execute(new Runnable() {
            public void run() {
                isNull.set(bootstrap.getTaskInsideTransaction() == null);
            }
        });
        return isNull.get();
    }

    private boolean taskCanBeTakenExactlyAfterDelay(final int delay) {
        clock.addTime(delay - 1);
        specify(thereAreNoExecutableTasksRightNow());
        clock.addTime(1);
        specify(taskMayBeTakenRightNow());
        return true;
    }

    private boolean _cancelsSuccessfully(Future<?> f) {
        specify(!f.isDone());
        specify(!f.isCancelled());
        boolean success = f.cancel(false);
        specify(success);
        specify(f.isDone());
        specify(f.isCancelled());
        return true;
    }

    // Utility methods which expect a task context (prefixed with "_")

    private void _saveFuture(Future<?> future) {
        specify(future, should.not().equal(null));
        bindings.get().update("future", new DummyEntity(future));
    }

    private ScheduledFuture<?> _loadFuture() {
        DummyEntity tmp = (DummyEntity) bindings.get().read("future");
        return (ScheduledFuture<?>) tmp.getOther();
    }


    public class WhenNoTasksHaveBeenSubmitted {

        public void noTasksAreQueued() {
            specify(scheduler.getQueuedTasks(), should.equal(0));
        }

        public void anExecutorWillWaitUntilThereAreTasks() {
            specify(thereAreNoExecutableTasksRightNow());
        }

        public void afterRestartNoTasksAreQueued() {
            scheduler = new TaskSchedulerImpl(tx, clock, taskContext, rsf);
            scheduler.start();
            specify(scheduler.getQueuedTasks(), should.equal(0));
        }
    }

    public class WhenATaskIsSubmitted {

        public void create() {
            taskContext.execute(new Runnable() {
                public void run() {
                    Future<?> f = scheduler.submit(task1);
                    _saveFuture(f);
                }
            });
        }

        public void theTaskIsNotDone() {
            taskContext.execute(new Runnable() {
                public void run() {
                    Future<?> f = _loadFuture();
                    specify(f.isDone(), should.equal(false));
                    specify(f.isCancelled(), should.equal(false));
                }
            });
        }

        public void theTaskIsQueued() {
            specify(scheduler.getQueuedTasks(), should.equal(1));
        }

        public void anExecutorMayTakeTheTask() {
            final TaskBootstrap bootstrap = takeNextTaskFrom(scheduler);
            taskContext.execute(new Runnable() {
                public void run() {
                    specify(bootstrap.getTaskInsideTransaction(), should.equal(task1));
                }
            });
        }

        public void afterRestartTheTaskIsStillQueued() {
            scheduler = new TaskSchedulerImpl(tx, clock, taskContext, rsf);
            scheduler.start();
            specify(scheduler.getQueuedTasks(), should.equal(1));
        }
    }

    public class WhenATaskIsTakenFromTheQueue {

        public void create() {
            taskContext.execute(new Runnable() {
                public void run() {
                    Future<?> f = scheduler.submit(task1);
                    _saveFuture(f);
                }
            });
            specify(scheduler.getQueuedTasks(), should.equal(1));
            final TaskBootstrap bootstrap = takeNextTaskFrom(scheduler);
            taskContext.execute(new Runnable() {
                public void run() {
                    bootstrap.getTaskInsideTransaction();
                }
            });
        }

        public void theTaskIsDone() {
            taskContext.execute(new Runnable() {
                public void run() {
                    Future<?> f = _loadFuture();
                    specify(f.isDone());
                    specify(f.isCancelled(), should.equal(false));
                }
            });
        }

        public void noTasksAreQueued() {
            specify(scheduler.getQueuedTasks(), should.equal(0));
        }

        public void afterRestartNoTasksAreQueued() {
            scheduler = new TaskSchedulerImpl(tx, clock, taskContext, rsf);
            scheduler.start();
            specify(scheduler.getQueuedTasks(), should.equal(0));
        }
    }

    public class WhenATaskIsScheduled {

        public void create() {
            taskContext.execute(new Runnable() {
                public void run() {
                    // We use seconds here as the time unit, to test that
                    // they are converted correctly to milliseconds.
                    ScheduledFuture<?> f = scheduler.schedule(task1, 1, TimeUnit.SECONDS);
                    _saveFuture(f);
                }
            });
        }

        public void theTaskIsQueued() {
            specify(scheduler.getQueuedTasks(), should.equal(1));
        }

        public void anExecutorCanNotTakeItBeforeTheDelay() {
            specify(thereAreNoExecutableTasksRightNow());
            specify(scheduler.getQueuedTasks(), should.equal(1));
        }

        public void anExecutorMayTakeItAfterTheDelay() {
            clock.addTime(1000);
            specify(taskMayBeTakenRightNow());
            specify(scheduler.getQueuedTasks(), should.equal(0));
        }

        public void afterRestartAnExecutorCanNotTakeItBeforeTheDelay() {
            scheduler = new TaskSchedulerImpl(tx, clock, taskContext, rsf);
            scheduler.start();
            specify(scheduler.getQueuedTasks(), should.equal(1));
            anExecutorCanNotTakeItBeforeTheDelay();
        }

        public void afterRestartAnExecutorMayTakeItAfterTheDelay() {
            scheduler = new TaskSchedulerImpl(tx, clock, taskContext, rsf);
            scheduler.start();
            specify(scheduler.getQueuedTasks(), should.equal(1));
            anExecutorMayTakeItAfterTheDelay();
        }

        public void anotherTaskWithAShorterDelayWillBeExecutedFirst() {
            taskContext.execute(new Runnable() {
                public void run() {
                    ScheduledFuture<?> f1 = _loadFuture();
                    ScheduledFuture<?> f2 = scheduler.schedule(task2, 500, TimeUnit.MILLISECONDS);

                    ScheduledFuture<?>[] executionOrder = {f1, f2};
                    Arrays.sort(executionOrder);
                    specify(executionOrder, should.containInOrder(f2, f1));
                }
            });
            clock.addTime(2000);
            final TaskBootstrap bootstrap1 = takeNextTaskFrom(scheduler);
            final TaskBootstrap bootstrap2 = takeNextTaskFrom(scheduler);
            taskContext.execute(new Runnable() {
                public void run() {
                    specify(bootstrap1.getTaskInsideTransaction(), should.equal(task2));
                    specify(bootstrap2.getTaskInsideTransaction(), should.equal(task1));
                }
            });
        }
    }

    public class WhenATaskIsScheduledAtFixedRate {

        public void create() {
            taskContext.execute(new Runnable() {
                public void run() {
                    // We use seconds here as the time unit, to test that
                    // they are converted correctly to milliseconds.
                    scheduler.scheduleAtFixedRate(task1, 1, 2, TimeUnit.SECONDS);
                }
            });
        }

        public void theFirstExecutionIsAfterTheInitialDelay() {
            specify(taskCanBeTakenExactlyAfterDelay(1000));
        }

        public void theFollowingExecutionsAreAtTheFixedRate() {
            specify(taskCanBeTakenExactlyAfterDelay(1000));
            specify(taskCanBeTakenExactlyAfterDelay(2000));
            specify(taskCanBeTakenExactlyAfterDelay(2000));
        }

        public void whenAnExecutionIsLateThenTheDelayRateOfExecutionsIsFixed() {
            clock.addTime(1200);
            specify(taskMayBeTakenRightNow());
            specify(taskCanBeTakenExactlyAfterDelay(1800));
        }
    }

    public class WhenATaskIsScheduledWithFixedDelay {

        public void create() {
            taskContext.execute(new Runnable() {
                public void run() {
                    // We use seconds here as the time unit, to test that
                    // they are converted correctly to milliseconds.
                    scheduler.scheduleWithFixedDelay(task1, 1, 2, TimeUnit.SECONDS);
                }
            });
        }

        public void theFirstExecutionIsAfterTheInitialDelay() {
            specify(taskCanBeTakenExactlyAfterDelay(1000));
        }

        public void theFollowingExecutionsAreAfterTheFixedDelay() {
            specify(taskCanBeTakenExactlyAfterDelay(1000));
            specify(taskCanBeTakenExactlyAfterDelay(2000));
            specify(taskCanBeTakenExactlyAfterDelay(2000));
        }

        public void whenAnExecutionIsLateThenTheDelayBetweenTaskExecutionsIsFixed() {
            clock.addTime(1200);
            specify(taskMayBeTakenRightNow());
            specify(taskCanBeTakenExactlyAfterDelay(2000));
        }
    }

    public class WhenATaskIsCancelledImmediatelyAfterSubmittingIt {

        public void create() {
            taskContext.execute(new Runnable() {
                public void run() {
                    // Use each variation of scheduling when testing cancelling
                    ScheduledFuture<?> f = scheduler.schedule(task1, 1, TimeUnit.SECONDS);
                    specify(f.getDelay(TimeUnit.SECONDS), should.equal(1));
                    specify(_cancelsSuccessfully(f));
                }
            });
        }

        public void theTaskIsCancelled() {
            clock.addTime(1000);
            specify(thereAreNoExecutableTasksRightNow());
            specify(scheduler.getQueuedTasks(), should.equal(0));
        }
    }

    public class WhenAScheduledTaskIsCancelledBeforeItIsExecuted {

        public void create() {
            taskContext.execute(new Runnable() {
                public void run() {
                    // Use each variation of scheduling when testing cancelling
                    ScheduledFuture<?> f = scheduler.scheduleAtFixedRate(task1, 1, 2, TimeUnit.SECONDS);
                    _saveFuture(f);
                }
            });
            clock.addTime(100);
            taskContext.execute(new Runnable() {
                public void run() {
                    ScheduledFuture<?> f = _loadFuture();
                    specify(f.getDelay(TimeUnit.MILLISECONDS), should.equal(900));
                    specify(_cancelsSuccessfully(f));
                }
            });
        }

        public void theTaskIsCancelled() {
            clock.addTime(900);
            specify(thereAreNoExecutableTasksRightNow());
            specify(scheduler.getQueuedTasks(), should.equal(0));
        }
    }

    public class WhenARepeatingScheduledTaskIsCancelledDuringItIsExecuted {

        public void create() {
            taskContext.execute(new Runnable() {
                public void run() {
                    // Use each variation of scheduling when testing cancelling
                    ScheduledFuture<?> f = scheduler.scheduleWithFixedDelay(task1, 1, 2, TimeUnit.SECONDS);
                    _saveFuture(f);
                }
            });
            clock.addTime(1000);
            final TaskBootstrap bootstrap = takeNextTaskFrom(scheduler);
            taskContext.execute(new Runnable() {
                public void run() {
                    specify(bootstrap.getTaskInsideTransaction(), should.equal(task1));
                    ScheduledFuture<?> f = _loadFuture();
                    specify(f.getDelay(TimeUnit.MILLISECONDS), should.equal(2000));
                    specify(_cancelsSuccessfully(f));
                }
            });
        }

        public void theTaskIsCancelled() {
            clock.addTime(2000);
            specify(thereAreNoExecutableTasksRightNow());
            specify(scheduler.getQueuedTasks(), should.equal(0));
        }
    }
}
