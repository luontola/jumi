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
import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.context.*;
import net.orfjackal.dimdwarf.tasks.*;
import net.orfjackal.dimdwarf.util.StubProvider;
import org.junit.runner.RunWith;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Esko Luontola
 * @since 26.11.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class TaskThreadPoolSpec extends Specification<Object> {

    private Context taskContext;
    private BlockingQueue<TaskBootstrap> taskQueue;
    private TaskThreadPool pool;

    public void create() throws Exception {
        taskContext = mock(Context.class);
        TaskExecutor executor = new TaskExecutor(
                new StubProvider<Context>(taskContext),
                new Provider<FilterChain>() {
                    public FilterChain get() {
                        return new FilterChain(new Filter[0]);
                    }
                });

        taskQueue = new LinkedBlockingDeque<TaskBootstrap>();
        TaskProducer producer = new TaskProducer() {
            public TaskBootstrap takeNextTask() throws InterruptedException {
                return taskQueue.take();
            }
        };

        pool = new TaskThreadPool(producer, executor);
        pool.start();
    }


    public class WhenTasksAreAddedToTheQueue {

        private CountDownLatch end = new CountDownLatch(1);
        private AtomicBoolean taskWasExecuted = new AtomicBoolean(false);
        private AtomicBoolean bootstrapWasInsideTaskContext = new AtomicBoolean(false);
        private AtomicBoolean executionWasInsideTaskContext = new AtomicBoolean(false);

        public void create() throws InterruptedException {
            final Runnable task = new Runnable() {
                public void run() {
                    executionWasInsideTaskContext.set(ThreadContext.getCurrentContext() == taskContext);
                    taskWasExecuted.set(true);
                    end.countDown();
                }
            };
            taskQueue.add(new TaskBootstrap() {
                public Runnable getTaskInsideTransaction() {
                    bootstrapWasInsideTaskContext.set(ThreadContext.getCurrentContext() == taskContext);
                    return task;
                }
            });
            end.await(50, TimeUnit.MILLISECONDS);
        }

        public void theyAreExecuted() throws InterruptedException {
            specify(taskWasExecuted.get());
        }

        public void theyAreBootstrappedInsideTaskContext() {
            specify(bootstrapWasInsideTaskContext.get());
        }

        public void theyAreExecutedInsideTaskContext() throws InterruptedException {
            specify(executionWasInsideTaskContext.get());
        }
    }

    public class WhenManyTasksAreAddedToTheQueueConcurrently {

        private CountDownLatch step1 = new CountDownLatch(1);
        private CountDownLatch step2 = new CountDownLatch(1);
        private CountDownLatch step3 = new CountDownLatch(1);

        public void create() {
            Runnable task1 = new Runnable() {
                public void run() {
                    try {
                        step1.countDown();
                        step2.await();
                        step3.countDown();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            Runnable task2 = new Runnable() {
                public void run() {
                    try {
                        step1.await();
                        step2.countDown();
                        step3.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            taskQueue.add(new SimpleTaskBootstrap(task1));
            taskQueue.add(new SimpleTaskBootstrap(task2));
        }

        public void theyAreExecutedInParallel() throws InterruptedException {
            specify(step1.await(50, TimeUnit.MILLISECONDS));
            specify(step2.await(50, TimeUnit.MILLISECONDS));
            specify(step3.await(50, TimeUnit.MILLISECONDS));
        }
    }


    private static class SimpleTaskBootstrap implements TaskBootstrap {
        private final Runnable task;

        public SimpleTaskBootstrap(Runnable task) {
            this.task = task;
        }

        public Runnable getTaskInsideTransaction() {
            return task;
        }
    }

    // TODO: shuts down cleanly
    // TODO: knows which tasks are executing and can tell when all currently executing tasks have finished (needed for GC)

    // TODO: give access to the current task's ScheduledFuture?
}
