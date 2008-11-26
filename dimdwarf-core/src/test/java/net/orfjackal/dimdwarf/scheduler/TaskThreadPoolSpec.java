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
import org.jmock.Expectations;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

import java.util.concurrent.*;

/**
 * @author Esko Luontola
 * @since 26.11.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class TaskThreadPoolSpec extends Specification<Object> {

    private static final int TEST_TIMEOUT = 50;

    private Context taskContext;
    private BlockingQueue<TaskBootstrap> taskQueue;
    private Logger logger;
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
        logger = mock(Logger.class);

        pool = new TaskThreadPool(executor, producer, logger);
        pool.start();
    }


    public class WhenTasksAreAddedToTheQueue {

        private CountDownLatch end = new CountDownLatch(1);

        private volatile boolean taskWasExecuted = false;
        private volatile boolean bootstrapWasInsideTaskContext = false;
        private volatile boolean executionWasInsideTaskContext = false;

        public void create() throws InterruptedException {
            final Runnable task = new Runnable() {
                public void run() {
                    executionWasInsideTaskContext = (ThreadContext.getCurrentContext() == taskContext);
                    taskWasExecuted = true;
                    end.countDown();
                }
            };
            taskQueue.add(new TaskBootstrap() {
                public Runnable getTaskInsideTransaction() {
                    bootstrapWasInsideTaskContext = (ThreadContext.getCurrentContext() == taskContext);
                    return task;
                }
            });
            end.await(TEST_TIMEOUT, TimeUnit.MILLISECONDS);
        }

        public void theyAreExecuted() throws InterruptedException {
            specify(taskWasExecuted);
        }

        public void theyAreBootstrappedInsideTaskContext() {
            specify(bootstrapWasInsideTaskContext);
        }

        public void theyAreExecutedInsideTaskContext() throws InterruptedException {
            specify(executionWasInsideTaskContext);
        }
    }

    public class WhenManyTasksAreAddedToTheQueueConcurrently {

        private CountDownLatch step1 = new CountDownLatch(1);
        private CountDownLatch step2 = new CountDownLatch(1);
        private CountDownLatch step3 = new CountDownLatch(1);
        private CountDownLatch stepEnd = new CountDownLatch(1);

        private volatile Integer runningTasks0 = null;
        private volatile Integer runningTasks1 = null;
        private volatile Integer runningTasks2 = null;
        private volatile Integer runningTasksEnd = null;

        public void create() throws InterruptedException {
            Runnable task1 = new Runnable() {
                public void run() {
                    try {
                        runningTasks1 = pool.getRunningTasks();
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
                        runningTasks2 = pool.getRunningTasks();
                        step2.countDown();
                        step3.await();
                        stepEnd.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            runningTasks0 = pool.getRunningTasks();
            taskQueue.add(new SimpleTaskBootstrap(task1));

            step1.await(TEST_TIMEOUT, TimeUnit.MILLISECONDS);
            taskQueue.add(new SimpleTaskBootstrap(task2));

            step3.await(TEST_TIMEOUT, TimeUnit.MILLISECONDS);
            runningTasksEnd = pool.getRunningTasks();
            stepEnd.countDown();
        }

        public void theyAreExecutedInParallel() throws InterruptedException {
            specify(step1.await(TEST_TIMEOUT, TimeUnit.MILLISECONDS));
            specify(step2.await(TEST_TIMEOUT, TimeUnit.MILLISECONDS));
            specify(step3.await(TEST_TIMEOUT, TimeUnit.MILLISECONDS));
        }

        public void thePoolKnowsTheNumberOfRunningTasks() {
            specify(runningTasks0, should.equal(0));
            specify(runningTasks1, should.equal(1));
            specify(runningTasks2, should.equal(2));
            specify(runningTasksEnd, runningTasksEnd >= 0);
            specify(runningTasksEnd, runningTasksEnd <= 1);
        }
    }

    public class WhenATaskFails {

        private CountDownLatch end = new CountDownLatch(1);
        private RuntimeException exception = new RuntimeException("Dummy exception");

        public void create() throws InterruptedException {
            checking(theExceptionIsLogged());
            Runnable task = new Runnable() {
                public void run() {
                    end.countDown();
                    throw exception;
                }
            };
            taskQueue.add(new SimpleTaskBootstrap(task));
            end.await(TEST_TIMEOUT, TimeUnit.MILLISECONDS);
        }

        public Expectations theExceptionIsLogged() {
            return new Expectations() {{
                one(logger).error("Task threw an exception", exception);
            }};
        }

        public void theNumberOfRunningTasksIsDecrementedCorrectly() {
            specify(pool.getRunningTasks(), should.equal(0));
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
