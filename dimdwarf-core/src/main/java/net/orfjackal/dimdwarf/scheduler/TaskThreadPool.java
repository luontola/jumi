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

import com.google.inject.Inject;
import net.orfjackal.dimdwarf.tasks.TaskExecutor;
import org.slf4j.*;

import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author Esko Luontola
 * @since 26.11.2008
 */
//@Singleton // TODO: a test must fail first
@ThreadSafe
public class TaskThreadPool {
    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(TaskThreadPool.class);
    private final Logger logger;

    private final TaskExecutor taskContext;
    private final TaskProducer producer;
    private final Thread consumer;
    private final ExecutorService workers;
    private final Set<CountDownLatch> runningTasks = Collections.synchronizedSet(new HashSet<CountDownLatch>());

    @Inject
    public TaskThreadPool(TaskExecutor taskContext, TaskProducer producer, ExecutorService threadPool) {
        this(taskContext, producer, threadPool, DEFAULT_LOGGER);
    }

    public TaskThreadPool(TaskExecutor taskContext, TaskProducer producer, ExecutorService threadPool, Logger logger) {
        this.taskContext = taskContext;
        this.producer = producer;
        this.consumer = new Thread(new TaskConsumer(), "Consume Scheduled Tasks");
        this.workers = threadPool;
        this.logger = logger;
    }

    public void start() {
        consumer.start();
    }

    public int getRunningTasks() {
        return runningTasks.size();
    }

    public void shutdown() {
        logger.info("Shutting down...");
        consumer.interrupt();
        try {
            consumer.join();
        } catch (InterruptedException e) {
            logger.error("Interrupted while shutting down", e);
        }
        workers.shutdown();
        logger.info("Shutdown finished");
    }

    @SuppressWarnings({"ToArrayCallWithZeroLengthArrayArgument"})
    public void awaitForCurrentTasksToFinish() throws InterruptedException {
        // It would be dangerous to pass an array larger than 0 to the toArray() method,
        // because there is a small chance that between the calls to size() and toArray()
        // an entry is removed from the collection, and the returned array would be too
        // big and would contain a null entry (toArray() does not shrink the array parameter
        // if it's too big).
        CountDownLatch[] snapshotOfRunningTasks = runningTasks.toArray(new CountDownLatch[0]);
        for (CountDownLatch taskHasFinished : snapshotOfRunningTasks) {
            taskHasFinished.await();
        }
    }


    private class TaskConsumer implements Runnable {
        public void run() {
            while (!Thread.interrupted()) {
                try {
                    TaskBootstrap bootstrap = producer.takeNextTask();
                    workers.submit(new TaskContextSetup(new Bootstrapper(bootstrap)));
                } catch (InterruptedException e) {
                    logger.info("Task consumer was interrupted", e);
                    return;
                }
            }
        }
    }

    private class TaskContextSetup implements Runnable {
        private final Runnable task;

        public TaskContextSetup(Runnable task) {
            this.task = task;
        }

        public void run() {
            CountDownLatch taskHasFinished = new CountDownLatch(1);
            try {
                runningTasks.add(taskHasFinished);
                taskContext.execute(task);
            } catch (Throwable t) {
                logger.error("Task threw an exception", t);
            } finally {
                runningTasks.remove(taskHasFinished);
                taskHasFinished.countDown();
            }
        }
    }

    private static class Bootstrapper implements Runnable {
        private final TaskBootstrap bootstrap;

        public Bootstrapper(TaskBootstrap bootstrap) {
            this.bootstrap = bootstrap;
        }

        public void run() {
            Runnable task = bootstrap.getTaskInsideTransaction();
            task.run();
        }
    }
}
