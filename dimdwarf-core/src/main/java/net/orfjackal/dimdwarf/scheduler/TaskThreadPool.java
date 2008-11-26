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

import net.orfjackal.dimdwarf.tasks.TaskExecutor;

import java.util.concurrent.*;

/**
 * @author Esko Luontola
 * @since 26.11.2008
 */
public class TaskThreadPool {

    private final ExecutorService workers;
    private final Thread consumer;

    public TaskThreadPool(TaskProducer producer, TaskExecutor executor) {
        workers = Executors.newCachedThreadPool();
        consumer = new Thread(new Consumer(producer, executor));
        consumer.start();
    }

    private class Consumer implements Runnable {
        private final TaskProducer producer;
        private final TaskExecutor taskContext;

        public Consumer(TaskProducer producer, TaskExecutor taskContext) {
            this.producer = producer;
            this.taskContext = taskContext;
        }

        public void run() {
            while (!Thread.interrupted()) {
                try {
                    TaskBootstrap bootstrap = producer.takeNextTask();
                    taskContext.execute(new BootstrappingTaskRunner(bootstrap));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class BootstrappingTaskRunner implements Runnable {
        private final TaskBootstrap bootstrap;

        public BootstrappingTaskRunner(TaskBootstrap bootstrap) {
            this.bootstrap = bootstrap;
        }

        public void run() {
            Runnable task = bootstrap.getTaskInsideTransaction();
            task.run();
        }
    }
}
