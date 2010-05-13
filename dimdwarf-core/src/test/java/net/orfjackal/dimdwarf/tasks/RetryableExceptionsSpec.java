// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.tasks;

import com.google.inject.*;
import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.api.EntityId;
import net.orfjackal.dimdwarf.api.internal.EntityObjectId;
import net.orfjackal.dimdwarf.db.Blob;
import net.orfjackal.dimdwarf.entities.dao.EntityDao;
import net.orfjackal.dimdwarf.modules.CommonModules;
import net.orfjackal.dimdwarf.server.TestServer;
import org.junit.runner.RunWith;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

@RunWith(JDaveRunner.class)
@Group({"fast"})
public class RetryableExceptionsSpec extends Specification<Object> {

    // FIXME: retryable exceptions will be removed/refactored in new architecture

    private static final EntityId ID1 = new EntityObjectId(1);

    private TestServer server;
    private Executor taskContext;
    private Provider<EntityDao> entities;

    public void create() throws Exception {
        server = new TestServer(
                new CommonModules()
        );
        server.changeLoggingLevel(TransactionFilter.class, Level.WARNING);
        server.changeLoggingLevel(RetryingTaskExecutor.class, Level.WARNING);

        Injector injector = server.getInjector();
        taskContext = injector.getInstance(RetryingTaskExecutor.class);
        entities = injector.getProvider(EntityDao.class);
    }

    public void destroy() throws Exception {
        server.shutdownIfRunning();
    }

    private static void countDownAndAwait(CountDownLatch latch) {
        try {
            latch.countDown();
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public class WhenATaskFailsDueToAnException {
        private AtomicInteger runCount = new AtomicInteger(0);

        public void exceptionsFromProgrammingErrorsDoNotCauseRetrying() {
            specify(new Block() {
                public void run() throws Throwable {

                    taskContext.execute(new Runnable() {
                        public void run() {
                            runCount.incrementAndGet();
                            throw new RuntimeException("dummy exception");
                        }
                    });
                }
            }, should.raise(GivenUpOnTaskException.class));
            specify(runCount.get(), should.equal(1));
        }

        public void exceptionsFromOptimisticLockingConflictCauseRetrying() throws InterruptedException {
            final CountDownLatch bothTasksRunning = new CountDownLatch(2);
            final Blob value1 = Blob.fromBytes(new byte[]{1});
            final Blob value2 = Blob.fromBytes(new byte[]{2});

            final Runnable task1 = new Runnable() {
                public void run() {
                    runCount.incrementAndGet();
                    countDownAndAwait(bothTasksRunning);
                    entities.get().update(ID1, value1);
                }
            };
            Runnable task2 = new Runnable() {
                public void run() {
                    runCount.incrementAndGet();
                    countDownAndAwait(bothTasksRunning);
                    entities.get().update(ID1, value2);
                }
            };

            Thread conflictingThread = new Thread(new Runnable() {
                public void run() {
                    taskContext.execute(task1);
                }
            });
            conflictingThread.start();
            taskContext.execute(task2);
            conflictingThread.join();

            // One of the tasks succeeds on first try, and the other task succeeds on second try.
            // So the tasks are run a total of 3 times, of which one transaction is rolled back.
            specify(runCount.get(), should.equal(3));
        }
    }
}
