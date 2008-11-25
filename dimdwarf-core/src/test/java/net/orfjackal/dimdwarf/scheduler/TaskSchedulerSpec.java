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
import net.orfjackal.dimdwarf.entities.*;
import net.orfjackal.dimdwarf.modules.*;
import net.orfjackal.dimdwarf.tasks.TaskExecutor;
import net.orfjackal.dimdwarf.util.*;
import org.junit.runner.RunWith;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * @author Esko Luontola
 * @since 24.11.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class TaskSchedulerSpec extends Specification<Object> {

    private static final int THREAD_SYNC_DELAY = 5;

    private TaskSchedulerImpl scheduler;
    private Provider<BindingStorage> bindings;
    private Provider<EntityInfo> entities;
    private TaskExecutor taskContext;
    private DummyClock clock;

    private DummyTask task1;
    private DummyTask task2;

    public void create() {
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
        entities = injector.getProvider(EntityInfo.class);
        taskContext = injector.getInstance(TaskExecutor.class);
        specify(thereMayBeBindingsInOtherNamespaces());

        scheduler = new TaskSchedulerImpl(bindings, entities, clock, taskContext);
        task1 = new DummyTask("1");
        task2 = new DummyTask("2");
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

    private boolean taskCanBeTakenRightNow() {
        specify(takeNextTaskFrom(scheduler), should.not().equal(null));
        return true;
    }

    private static DummyTask takeNextTaskFrom(TaskSchedulerImpl scheduler) {
        try {
            return (DummyTask) scheduler.takeNextTask();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean taskCanBeTakenExactlyAfterDelay(int delay) {
        clock.addTime(delay - 1);
        interruptTestThreadAfter(THREAD_SYNC_DELAY);
        specify(takeNextTaskIsInterrupted());
        clock.addTime(1);
        specify(taskCanBeTakenRightNow());
        return true;
    }

    private static void interruptTestThreadAfter(final int delay) {
        final Thread testThread = Thread.currentThread();
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                testThread.interrupt();
            }
        }).start();
    }

    private boolean takeNextTaskIsInterrupted() {
        specify(new Block() {
            public void run() throws Throwable {
                scheduler.takeNextTask();
            }
        }, should.raise(InterruptedException.class));
        return true;
    }


    public class WhenNoTasksHaveBeenSubmitted {

        public void noTasksAreQueued() {
            specify(scheduler.getQueuedTasks(), should.equal(0));
        }

        public void anExecutorWillWaitUntilThereAreTasks() {
            interruptTestThreadAfter(THREAD_SYNC_DELAY);
            specify(takeNextTaskIsInterrupted());
        }

        public void afterRestartNoTasksAreQueued() {
            scheduler = new TaskSchedulerImpl(bindings, entities, clock, taskContext);
            specify(scheduler.getQueuedTasks(), should.equal(0));
        }
    }

    public class WhenATaskIsSubmitted {

        public void create() {
            taskContext.execute(new Runnable() {
                public void run() {
                    scheduler.submit(task1);
                }
            });
        }

        public void theTaskIsQueued() {
            specify(scheduler.getQueuedTasks(), should.equal(1));
        }

        public void anExecutorMayTakeTheTask() {
            taskContext.execute(new Runnable() {
                public void run() {
                    specify(takeNextTaskFrom(scheduler), should.equal(task1));
                }
            });
        }

        public void afterRestartTheTaskIsStillQueued() {
            scheduler = new TaskSchedulerImpl(bindings, entities, clock, taskContext);
            specify(scheduler.getQueuedTasks(), should.equal(1));
        }

        // TODO: if transaction rolls back, the task should not be executed
        // TODO: the task should not be executed *before* the task commits
    }

    public class WhenATaskIsTakenFromTheQueue {

        public void create() {
            taskContext.execute(new Runnable() {
                public void run() {
                    scheduler.submit(task1);
                    takeNextTaskFrom(scheduler);
                }
            });
        }

        public void noTasksAreQueued() {
            specify(scheduler.getQueuedTasks(), should.equal(0));
        }

        public void afterRestartNoTasksAreQueued() {
            scheduler = new TaskSchedulerImpl(bindings, entities, clock, taskContext);
            specify(scheduler.getQueuedTasks(), should.equal(0));
        }

        // TODO: if transaction rolls back, the task should be rescheduled
    }

    public class WhenATaskIsScheduled {

        public void create() {
            taskContext.execute(new Runnable() {
                public void run() {
                    // We use seconds here as the time unit, to test that
                    // they are converted correctly to milliseconds.
                    scheduler.schedule(task1, 1, TimeUnit.SECONDS);
                }
            });
        }

        public void theTaskIsQueued() {
            specify(scheduler.getQueuedTasks(), should.equal(1));
        }

        public void anExecutorCanNotTakeItBeforeTheDelay() {
            interruptTestThreadAfter(THREAD_SYNC_DELAY);
            specify(takeNextTaskIsInterrupted());
            specify(scheduler.getQueuedTasks(), should.equal(1));
        }

        public void anExecutorMayTakeItAfterTheDelay() {
            taskContext.execute(new Runnable() {
                public void run() {
                    clock.addTime(1000);
                    specify(taskCanBeTakenRightNow());
                }
            });
            specify(scheduler.getQueuedTasks(), should.equal(0));
        }

        public void afterRestartAnExecutorCanNotTakeItBeforeTheDelay() {
            scheduler = new TaskSchedulerImpl(bindings, entities, clock, taskContext);
            specify(scheduler.getQueuedTasks(), should.equal(1));
            anExecutorCanNotTakeItBeforeTheDelay();
        }

        public void afterRestartAnExecutorMayTakeItAfterTheDelay() {
            scheduler = new TaskSchedulerImpl(bindings, entities, clock, taskContext);
            specify(scheduler.getQueuedTasks(), should.equal(1));
            anExecutorMayTakeItAfterTheDelay();
        }

        public void anotherTaskWithAShorterDelayWillBeExecutedFirst() {
            taskContext.execute(new Runnable() {
                public void run() {
                    scheduler.schedule(task2, 500, TimeUnit.MILLISECONDS);
                    clock.addTime(2000);
                    specify(takeNextTaskFrom(scheduler).value, should.equal("2"));
                    specify(takeNextTaskFrom(scheduler).value, should.equal("1"));
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
            taskContext.execute(new Runnable() {
                public void run() {
                    specify(taskCanBeTakenExactlyAfterDelay(1000));
                }
            });
        }

        public void theFollowingExecutionsAreAtTheFixedRate() {
            taskContext.execute(new Runnable() {
                public void run() {
                    specify(taskCanBeTakenExactlyAfterDelay(1000));
                    specify(taskCanBeTakenExactlyAfterDelay(2000));
                    specify(taskCanBeTakenExactlyAfterDelay(2000));
                }
            });
        }

        public void whenAnExecutionIsLateThenTheDelayRateOfExecutionsIsFixed() {
            taskContext.execute(new Runnable() {
                public void run() {
                    clock.addTime(1200);
                    specify(taskCanBeTakenRightNow());
                    specify(taskCanBeTakenExactlyAfterDelay(1800));
                }
            });
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
            taskContext.execute(new Runnable() {
                public void run() {
                    specify(taskCanBeTakenExactlyAfterDelay(1000));
                }
            });
        }

        public void theFollowingExecutionsAreAfterTheFixedDelay() {
            taskContext.execute(new Runnable() {
                public void run() {
                    specify(taskCanBeTakenExactlyAfterDelay(1000));
                    specify(taskCanBeTakenExactlyAfterDelay(2000));
                    specify(taskCanBeTakenExactlyAfterDelay(2000));
                }
            });
        }

        public void whenAnExecutionIsLateThenTheDelayBetweenTaskExecutionsIsFixed() {
            taskContext.execute(new Runnable() {
                public void run() {
                    clock.addTime(1200);
                    specify(taskCanBeTakenRightNow());
                    specify(taskCanBeTakenExactlyAfterDelay(2000));
                }
            });
        }
    }

    // TODO: cancelling tasks

    private static class DummyTask implements Runnable, Serializable {
        private static final long serialVersionUID = 1L;

        public String value;

        public DummyTask() {
        }

        public DummyTask(String value) {
            this.value = value;
        }

        public void run() {
        }
    }
}
