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

package net.orfjackal.dimdwarf.tasks;

import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.db.OptimisticLockException;
import net.orfjackal.dimdwarf.scheduler.DummyTask;
import net.orfjackal.dimdwarf.util.ThrowingRunnable;
import org.jmock.Expectations;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Esko Luontola
 * @since 18.1.2009
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class SingleThreadFallbackTaskExecutorSpec extends Specification<Object> {

    private SingleThreadFallbackTaskExecutor executor;
    private Executor backingExecutor;
    private Logger logger;

    private Exception nonRetryable = new GivenUpOnTaskException(new IllegalArgumentException());
    private Exception retryable = new GivenUpOnTaskException(new OptimisticLockException());

    private static void runInNewThread(final Runnable command, final Executor executor) {
        Thread t = new Thread(new Runnable() {
            public void run() {
                executor.execute(command);
            }
        });
        t.start();
    }


    public class WhenATaskIsExecuted {
        private DummyTask task = new DummyTask("A");

        public void create() {
            backingExecutor = mock(Executor.class);
            logger = mock(Logger.class);
            executor = new SingleThreadFallbackTaskExecutor(backingExecutor, logger);
        }

        public void ifItIsSuccessfulThenItIsExecutedOnce() {
            checking(new Expectations() {{
                one(backingExecutor).execute(task);
            }});
            executor.execute(task);
        }

        public void ifItFailsWithANonRetryableTaskThenItIsNotRetried() {
            checking(new Expectations() {{
                one(backingExecutor).execute(task); will(throwException(nonRetryable));
            }});
            specify(new Block() {
                public void run() throws Throwable {
                    executor.execute(task);
                }
            }, should.raise(GivenUpOnTaskException.class));
        }

        public void ifItFailsWithARetryableTaskThenItIsRetriedOnce() {
            checking(new Expectations() {{
                one(backingExecutor).execute(task); will(throwException(retryable));
                one(logger).info("Retrying task in single-threaded mode: " + task, retryable);
                one(backingExecutor).execute(task);
            }});
            executor.execute(task);
        }
    }

    public class WhenManyTasksAreExecutedConcurrently {
        private Runnable task1;
        private Runnable task2;
        private CountDownLatch task1Running = new CountDownLatch(1);
        private CountDownLatch task2Running = new CountDownLatch(1);
        private CountDownLatch task1Finished = new CountDownLatch(1);
        private CountDownLatch task2Finished = new CountDownLatch(1);

        public void create() {
            backingExecutor = new Executor() {
                public void execute(Runnable command) {
                    command.run();
                }
            };
            logger = dummy(Logger.class);
            executor = new SingleThreadFallbackTaskExecutor(backingExecutor, logger);
        }

        public void ifNoTasksFailThenTheyAreExecutedInParallel() throws InterruptedException {
            task1 = new ThrowingRunnable() {
                public void doRun() throws Throwable {
                    task1Running.countDown();
                    task2Running.await();
                    task1Finished.countDown();
                }
            };
            task2 = new ThrowingRunnable() {
                public void doRun() throws Throwable {
                    task2Running.countDown();
                    task1Finished.await();
                    task2Finished.countDown();
                }
            };
            runInNewThread(task1, executor);
            runInNewThread(task2, executor);
            task2Finished.await();
        }

        public void ifATaskFailsThenItIsRetriedInExclusiveSingleThreadedMode() throws InterruptedException {
            final AtomicBoolean wasExecutedSingleThreadedly = new AtomicBoolean(false);
            task1 = new ThrowingRunnable() {
                private int tries = 0;

                public void doRun() throws Throwable {
                    failOnFirstRun();
                    task1Running.countDown();
                    checkWhetherExecutedSingleThreadedly();
                    task1Finished.countDown();
                }

                private void failOnFirstRun() throws Exception {
                    if (++tries == 1) {
                        throw retryable;
                    }
                }

                private void checkWhetherExecutedSingleThreadedly() throws InterruptedException {
                    boolean timedOut = !task2Running.await(1, TimeUnit.MILLISECONDS);
                    wasExecutedSingleThreadedly.set(timedOut);
                }
            };
            task2 = new ThrowingRunnable() {
                public void doRun() throws Throwable {
                    task2Running.countDown();
                    task2Finished.countDown();
                }
            };
            runInNewThread(task1, executor);
            task1Running.await();
            runInNewThread(task2, executor);
            task2Finished.await();
            specify(wasExecutedSingleThreadedly.get());
        }
    }
}
