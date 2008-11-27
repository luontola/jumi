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
import net.orfjackal.dimdwarf.api.TaskScheduler;
import net.orfjackal.dimdwarf.modules.CommonModules;
import net.orfjackal.dimdwarf.server.ServerLifecycleManager;
import net.orfjackal.dimdwarf.tasks.TaskExecutor;
import org.junit.runner.RunWith;

import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * @author Esko Luontola
 * @since 27.11.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class TaskSchedulingIntegrationSpec extends Specification<Object> {

    private TaskExecutor taskContext;
    private Provider<TaskScheduler> scheduler;
    private TaskThreadPool pool;
    private TestSpy spy;

    private Provider<ServerLifecycleManager> server;

    public void create() throws Exception {
        Injector injector = Guice.createInjector(new CommonModules());
        taskContext = injector.getInstance(TaskExecutor.class);
        scheduler = injector.getProvider(TaskScheduler.class);
        pool = injector.getInstance(TaskThreadPool.class);
        spy = injector.getInstance(TestSpy.class);

        server = injector.getProvider(ServerLifecycleManager.class);
        server.get().start();
    }

    public void destroy() throws Exception {
        server.get().shutdown();
    }

    public class WhenAOneTimeTasksIsScheduled {

        public void create() throws InterruptedException {
            taskContext.execute(new Runnable() {
                public void run() {
                    scheduler.get().submit(new ExecutionLoggingTask("A"));
                }
            });
            spy.executionCount.acquire(1);
        }

        public void itIsExecutedOnce() {
            specify(spy.executions, should.containInOrder("A:1"));
        }
    }


    @Singleton
    public static class TestSpy {

        public final Semaphore executionCount = new Semaphore(0);
        public final List<String> executions = Collections.synchronizedList(new ArrayList<String>());

        public void logExecution(String dummyId, int count) {
            executions.add(dummyId + ":" + count);
            executionCount.release();
        }
    }

    private static class ExecutionLoggingTask extends DummyTask {

        @Inject public transient TestSpy spy;
        private int myExecutionCount = 0;

        public ExecutionLoggingTask(String dummyId) {
            super(dummyId);
        }

        public void run() {
            myExecutionCount++;
            spy.logExecution(getDummyId(), myExecutionCount);
            System.out.println("TaskSchedulingIntegrationSpec$ExecutionLoggingTask.run");
            System.out.println("getDummyId() = " + getDummyId());
            System.out.println("myExecutionCount = " + myExecutionCount);
        }
    }

    // TODO: configure Guice modules
    // TODO: to monitor tests, use a dummy object in @Singleton scope - the tasks can have it injected to them
}
