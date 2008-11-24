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

    private TaskScheduler scheduler;
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
        specifyThereMayBeBindingsInOtherNamespaces();

        scheduler = new TaskScheduler(bindings, entities, clock, taskContext);
        task1 = new DummyTask("1");
        task2 = new DummyTask("2");
    }

    private void specifyThereMayBeBindingsInOtherNamespaces() {
        taskContext.execute(new Runnable() {
            public void run() {
                bindings.get().update("a.shouldNotTouchThis", new DummyEntity());
                bindings.get().update("z.shouldNotTouchThis", new DummyEntity());
            }
        });
    }

    private static DummyTask takeNextTaskFrom(TaskScheduler scheduler) {
        try {
            return (DummyTask) scheduler.takeNextTask();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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


    public class WhenNoTasksHaveBeenSubmitted {

        public void noTasksAreQueued() {
            specify(scheduler.getQueuedTasks(), should.equal(0));
        }

        public void anExecutorWillWaitUntilThereAreTasks() {
            interruptTestThreadAfter(THREAD_SYNC_DELAY);
            specify(new Block() {
                public void run() throws Throwable {
                    scheduler.takeNextTask();
                }
            }, should.raise(InterruptedException.class));
        }

        public void afterRestartNoTasksAreQueued() {
            scheduler = new TaskScheduler(bindings, entities, clock, taskContext);
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
            scheduler = new TaskScheduler(bindings, entities, clock, taskContext);
            specify(scheduler.getQueuedTasks(), should.equal(1));
        }
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
            scheduler = new TaskScheduler(bindings, entities, clock, taskContext);
            specify(scheduler.getQueuedTasks(), should.equal(0));
        }
    }

    public class WhenATaskIsScheduledWithADelay {

        public void create() {
            taskContext.execute(new Runnable() {
                public void run() {
                    scheduler.schedule(task1, 1000, TimeUnit.MILLISECONDS);
                }
            });
        }

        public void theTaskIsQueued() {
            specify(scheduler.getQueuedTasks(), should.equal(1));
        }

        public void anExecutorCanNotTakeItBeforeTheDelay() {
            interruptTestThreadAfter(THREAD_SYNC_DELAY);
            specify(new Block() {
                public void run() throws Throwable {
                    scheduler.takeNextTask();
                }
            }, should.raise(InterruptedException.class));
        }

        public void anExecutorMayTakeItAfterTheDelay() {
            clock.addTime(1000);
            taskContext.execute(new Runnable() {
                public void run() {
                    specify(takeNextTaskFrom(scheduler), should.not().equal(null));
                }
            });
        }

        public void afterRestartAnExecutorCanNotTakeItBeforeTheDelay() {
            scheduler = new TaskScheduler(bindings, entities, clock, taskContext);
            specify(scheduler.getQueuedTasks(), should.equal(1));
            anExecutorCanNotTakeItBeforeTheDelay();
        }

        public void afterRestartAnExecutorMayTakeItAfterTheDelay() {
            scheduler = new TaskScheduler(bindings, entities, clock, taskContext);
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

    private static class DummyTask implements Runnable, Serializable {

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
