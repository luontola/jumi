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

package net.orfjackal.dimdwarf.gc;

import com.google.inject.*;
import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.api.TaskScheduler;
import net.orfjackal.dimdwarf.modules.CommonModules;
import net.orfjackal.dimdwarf.server.TestServer;
import net.orfjackal.dimdwarf.tasks.TaskExecutor;
import org.junit.runner.RunWith;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author Esko Luontola
 * @since 10.12.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class IncrementalTaskSpec extends Specification<Object> {

    private TaskExecutor taskContext;
    private Provider<TaskScheduler> scheduler;
    private TestServer server;
    private TestSpy spy;

    public void create() throws Exception {
        server = new TestServer(new CommonModules());
        server.hideStartupShutdownLogs();
        server.start();

        Injector injector = server.getInjector();
        taskContext = injector.getInstance(TaskExecutor.class);
        scheduler = injector.getProvider(TaskScheduler.class);
        spy = injector.getInstance(TestSpy.class);
    }

    public void destroy() throws Exception {
        server.shutdownIfRunning();
    }


    public class WhenAnIncrementalTaskHasOnlyOneStep {

        public void create() throws InterruptedException {
            taskContext.execute(new Runnable() {
                public void run() {
                    scheduler.get().submit(
                            new IncrementalTaskRunner(
                                    new DummyIncrementalTask(1, 1),
                                    new DummyCallback()));
                }
            });
            spy.callbackCalled.await();
        }

        public void theTaskIsExecutedOnceAfterWhichTheCallbackIsRun() {
            specify(spy.executions, should.containInOrder("1", "callback"));
        }
    }

    public class WhenAnIncrementalTaskHasManySteps {

        public void create() throws InterruptedException {
            taskContext.execute(new Runnable() {
                public void run() {
                    scheduler.get().submit(
                            new IncrementalTaskRunner(
                                    new DummyIncrementalTask(1, 2),
                                    new DummyCallback()));
                }
            });
            spy.callbackCalled.await();
        }

        public void allTheStepsAreExecutedAfterWhichTheCallbackIsRun() {
            specify(spy.executions, should.containInOrder("1", "2", "callback"));
        }
    }

    public class WhenASequenceOfIncrementalTasksIsExecuted {

        public void create() throws InterruptedException {
            taskContext.execute(new Runnable() {
                public void run() {
                    scheduler.get().submit(
                            new IncrementalTaskRunner(
                                    new IncrementalTaskSequence(Arrays.asList(
                                            new DummyIncrementalTask(1, 2),
                                            new DummyIncrementalTask(5, 6))),
                                    new DummyCallback()));
                }
            });
            spy.callbackCalled.await();
        }

        public void theAllStepsOfTheFirstStageAreExecutedBeforeTheNextStage() {
            specify(spy.executions, should.containInOrder("1", "2", "5", "6", "callback"));
        }
    }


    private static class DummyIncrementalTask implements IncrementalTask, Serializable {

        @Inject public transient TestSpy spy;
        private final int current;
        private final int until;

        public DummyIncrementalTask(int current, int until) {
            this.current = current;
            this.until = until;
        }

        public Collection<? extends IncrementalTask> step() {
            spy.executions.add(String.valueOf(current));
            if (current < until) {
                return Arrays.asList(new DummyIncrementalTask(current + 1, until));
            } else {
                return Collections.emptyList();
            }
        }
    }

    private static class DummyCallback implements Runnable, Serializable {

        @Inject public transient TestSpy spy;

        public void run() {
            spy.executions.add("callback");
            spy.callbackCalled.countDown();
        }
    }

    @Singleton
    private static class TestSpy {

        public final List<String> executions = new CopyOnWriteArrayList<String>();
        public final CountDownLatch callbackCalled = new CountDownLatch(1);
    }
}
