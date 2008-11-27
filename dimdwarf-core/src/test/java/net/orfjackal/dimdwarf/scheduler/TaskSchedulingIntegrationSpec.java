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
import net.orfjackal.dimdwarf.db.inmemory.InMemoryDatabaseManager;
import net.orfjackal.dimdwarf.entities.EntityIdFactoryImpl;
import net.orfjackal.dimdwarf.modules.CommonModules;
import net.orfjackal.dimdwarf.server.ServerLifecycleManager;
import net.orfjackal.dimdwarf.tasks.TaskExecutor;
import org.junit.runner.RunWith;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author Esko Luontola
 * @since 27.11.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class TaskSchedulingIntegrationSpec extends Specification<Object> {

    private Injector injector;
    private TaskExecutor taskContext;
    private Provider<TaskScheduler> scheduler;
    private TestSpy spy;

    private Provider<ServerLifecycleManager> server;

    public void create() throws Exception {
        startupTheServer(new CommonModules());
    }

    private void startupTheServer(Module... modules) {
        injector = Guice.createInjector(modules);
        server = injector.getProvider(ServerLifecycleManager.class);
        server.get().start();

        taskContext = injector.getInstance(TaskExecutor.class);
        scheduler = injector.getProvider(TaskScheduler.class);
        spy = injector.getInstance(TestSpy.class);
    }

    private void shutdownTheServer() {
        server.get().shutdown();
    }

    private void restartTheServer() {
        shutdownTheServer();
        final InMemoryDatabaseManager db = injector.getInstance(InMemoryDatabaseManager.class);
        final EntityIdFactoryImpl idFactory = injector.getInstance(EntityIdFactoryImpl.class);

        startupTheServer(
                new CommonModules(),
                new AbstractModule() {
                    protected void configure() {
                        bind(InMemoryDatabaseManager.class).toInstance(db);
                        bind(EntityIdFactoryImpl.class).toInstance(idFactory);
                    }
                });
    }

    public void destroy() throws Exception {
        shutdownTheServer();
    }


    public class WhenAOneTimeTaskIsScheduled {

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

    public class WhenARepeatedTaskIsScheduled {

        public void create() throws InterruptedException {
            taskContext.execute(new Runnable() {
                public void run() {
                    scheduler.get().scheduleAtFixedRate(new ExecutionLoggingTask("A"), 0, 0, TimeUnit.MILLISECONDS);
                }
            });
            spy.executionCount.acquire(2);
        }

        public void itIsExecutedManyTimes() {
            specify(spy.executions, should.containAll("A:1", "A:2"));
        }

        public void afterShuttingDownItIsNoMoreExecuted() throws InterruptedException {
            shutdownTheServer();
            spy.executions.clear();
            Thread.sleep(10); // TODO: figure out a more reliable thread synchronization method than sleeping
            specify(spy.executions, should.containExactly());
        }

        public void afterRestartTheExecutionIsContinued() throws InterruptedException {
            restartTheServer();
            spy.executionCount.acquire(1);
            specify(spy.executions, should.not().containAny("A:1", "A:2"));
            specify(spy.executions, should.containAny("A:3", "A:4"));
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
            System.err.println("TaskSchedulingIntegrationSpec$ExecutionLoggingTask.run");
            System.err.println("getDummyId() = " + getDummyId());
            System.err.println("myExecutionCount = " + myExecutionCount);
        }
    }

    // TODO: configure Guice modules
    // TODO: to monitor tests, use a dummy object in @Singleton scope - the tasks can have it injected to them
}
