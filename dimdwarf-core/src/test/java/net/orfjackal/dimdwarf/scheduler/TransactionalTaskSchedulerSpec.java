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
import net.orfjackal.dimdwarf.api.EntityInfo;
import net.orfjackal.dimdwarf.entities.BindingStorage;
import net.orfjackal.dimdwarf.modules.*;
import static net.orfjackal.dimdwarf.scheduler.TaskSchedulerSpec.takeNextTaskFrom;
import net.orfjackal.dimdwarf.tasks.*;
import net.orfjackal.dimdwarf.tx.*;
import net.orfjackal.dimdwarf.util.*;
import org.jmock.Expectations;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;
import java.util.logging.*;

/**
 * @author Esko Luontola
 * @since 25.11.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class TransactionalTaskSchedulerSpec extends Specification<Object> {

    private TaskSchedulerImpl scheduler;
    private Provider<Transaction> tx;
    private TaskExecutor taskContext;
    private DummyClock clock;
    private Logger hideTransactionFailedLogs;

    private DummyTask task1 = new DummyTask("1");

    public void create() {
        clock = new DummyClock();
        Injector injector = Guice.createInjector(
                new EntityModule(),
                new DatabaseModule(),
                new TaskContextModule(),
                new AbstractModule() {
                    protected void configure() {
                        bind(Clock.class).toInstance(clock);
                    }
                });
        final Provider<BindingStorage> bindings = injector.getProvider(BindingStorage.class);
        final Provider<EntityInfo> entities = injector.getProvider(EntityInfo.class);
        tx = injector.getProvider(Transaction.class);
        taskContext = injector.getInstance(TaskExecutor.class);

        RecoverableSetFactory rsf = new RecoverableSetFactory() {
            public <T> RecoverableSet<T> create(String prefix) {
                return new RecoverableSetImpl<T>(prefix, bindings, entities);
            }
        };

        scheduler = new TaskSchedulerImpl(tx, clock, taskContext, rsf);

        hideTransactionFailedLogs = Logger.getLogger(TransactionFilter.class.getName());
        hideTransactionFailedLogs.setLevel(Level.SEVERE);
    }

    public void destroy() throws Exception {
        hideTransactionFailedLogs.setLevel(Level.ALL);
    }

    @SuppressWarnings("ThrowableInstanceNeverThrown")
    private TransactionParticipant failOnPrepare() {
        try {
            final TransactionParticipant failOnPrepare = mock(TransactionParticipant.class);
            checking(new Expectations() {{
                one(failOnPrepare).prepare(); will(throwException(new AssertionError("Dummy exception")));
                one(failOnPrepare).rollback();
            }});
            return failOnPrepare;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }


    public class WhenATaskIsSubmittedInATransaction {

        public void theTaskIsNotQueuedUntilTheTransactionCommits() {
            taskContext.execute(new Runnable() {
                public void run() {
                    scheduler.submit(task1);
                    specify(scheduler.getQueuedTasks(), should.equal(0));
                }
            });
            specify(scheduler.getQueuedTasks(), should.equal(1));
        }

        public void theTaskIsNotQueuedIfTheTransactionRollsBack() {
            specify(new Block() {
                public void run() throws Throwable {

                    taskContext.execute(new Runnable() {
                        public void run() {
                            scheduler.submit(task1);
                            tx.get().setRollbackOnly();
                        }
                    });
                }
            }, should.raise(TransactionException.class));
            specify(scheduler.getQueuedTasks(), should.equal(0));
        }

        public void theTaskIsNotQueuedIfTheTransactionRollsBackDuringPrepare() {
            specify(new Block() {
                public void run() throws Throwable {

                    taskContext.execute(new Runnable() {
                        public void run() {
                            scheduler.submit(task1);
                            tx.get().join(failOnPrepare());
                        }
                    });
                }
            }, should.raise(TransactionException.class));
            specify(scheduler.getQueuedTasks(), should.equal(0));
        }
    }

    public class WhenATaskIsTakenInATransaction {

        public void create() {
            taskContext.execute(new Runnable() {
                public void run() {
                    scheduler.submit(task1);
                }
            });
            specify(scheduler.getQueuedTasks(), should.equal(1));
        }

        public void theTaskIsIsRescheduledIfTheTransactionRollsBack() {
            specify(new Block() {
                public void run() throws Throwable {

                    final TaskBootstrap bootstrap = takeNextTaskFrom(scheduler);
                    taskContext.execute(new Runnable() {
                        public void run() {
                            specify(bootstrap.getTaskInsideTransaction(), should.equal(task1));
                            tx.get().setRollbackOnly();
                        }
                    });
                }
            }, should.raise(TransactionException.class));
            specify(scheduler.getQueuedTasks(), should.equal(1));
        }
    }

    public class WhenARepeatedTaskIsTakenInATransaction {

        public void create() {
            taskContext.execute(new Runnable() {
                public void run() {
                    scheduler.scheduleWithFixedDelay(task1, 1, 2, TimeUnit.SECONDS);
                }
            });
            specify(scheduler.getQueuedTasks(), should.equal(1));
        }

        public void theNextExecutionIsNotScheduledIfTheTransactionRollsBack() {
            clock.addTime(1000);
            specify(new Block() {
                public void run() throws Throwable {

                    final TaskBootstrap bootstrap = takeNextTaskFrom(scheduler);
                    taskContext.execute(new Runnable() {
                        public void run() {
                            specify(bootstrap.getTaskInsideTransaction(), should.equal(task1));
                            tx.get().setRollbackOnly();
                        }
                    });
                }
            }, should.raise(TransactionException.class));
            specify(scheduler.getQueuedTasks(), should.equal(1)); // the first task at T1000; can be taken right away

            final TaskBootstrap bootstrap = takeNextTaskFrom(scheduler);
            taskContext.execute(new Runnable() {
                public void run() {
                    specify(bootstrap.getTaskInsideTransaction(), should.equal(task1));
                }
            });
            specify(scheduler.getQueuedTasks(), should.equal(1)); // the next task at T1000+2000
        }
    }
}
