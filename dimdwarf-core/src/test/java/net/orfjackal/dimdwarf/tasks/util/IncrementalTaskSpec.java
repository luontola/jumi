// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.tasks.util;

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

@RunWith(JDaveRunner.class)
@Group({"fast"})
public class IncrementalTaskSpec extends Specification<Object> {

    private Executor taskContext;
    private Provider<TaskScheduler> scheduler;
    private TestServer server;
    private TestSpy spy;

    public void create() throws Exception {
        server = new TestServer(
                new CommonModules()
        );
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
