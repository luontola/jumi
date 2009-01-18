/*
 * This file is part of Dimdwarf Application Server <http://dimdwarf.sourceforge.net/>
 *
 * Copyright (c) 2008-2009, Esko Luontola. All Rights Reserved.
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

import com.google.inject.*;
import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.db.Blob;
import net.orfjackal.dimdwarf.entities.dao.EntityDao;
import net.orfjackal.dimdwarf.modules.CommonModules;
import net.orfjackal.dimdwarf.modules.options.NullGarbageCollectionOption;
import net.orfjackal.dimdwarf.server.TestServer;
import org.junit.runner.RunWith;

import java.math.BigInteger;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * @author Esko Luontola
 * @since 13.12.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class RetryableExceptionsSpec extends Specification<Object> {

    private TestServer server;
    private Executor taskContext;
    private Provider<EntityDao> entities;

    public void create() throws Exception {
        server = new TestServer(
                new CommonModules(),
                new NullGarbageCollectionOption()
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
                    entities.get().update(BigInteger.ONE, value1);
                }
            };
            Runnable task2 = new Runnable() {
                public void run() {
                    runCount.incrementAndGet();
                    countDownAndAwait(bothTasksRunning);
                    entities.get().update(BigInteger.ONE, value2);
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
