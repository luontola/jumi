// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

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

    // FIXME: SingleThreadFallbackTaskExecutor will be removed/refactored in new architecture

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
