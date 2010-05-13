// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.scheduler;

import com.google.inject.*;
import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.api.EntityInfo;
import net.orfjackal.dimdwarf.entities.BindingRepository;
import net.orfjackal.dimdwarf.modules.*;
import net.orfjackal.dimdwarf.tasks.*;
import net.orfjackal.dimdwarf.tx.*;
import net.orfjackal.dimdwarf.util.*;
import org.jmock.Expectations;
import org.junit.runner.RunWith;

import java.util.concurrent.*;
import java.util.logging.*;

import static net.orfjackal.dimdwarf.scheduler.TaskSchedulerSpec.takeNextTaskFrom;

@RunWith(JDaveRunner.class)
@Group({"fast"})
public class TransactionalTaskSchedulerSpec extends Specification<Object> {

    private TaskSchedulerImpl scheduler;
    private Provider<Transaction> tx;
    private Executor taskContext;
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
        final Provider<BindingRepository> bindings = injector.getProvider(BindingRepository.class);
        final Provider<EntityInfo> info = injector.getProvider(EntityInfo.class);
        tx = injector.getProvider(Transaction.class);
        taskContext = injector.getInstance(TaskExecutor.class);

        RecoverableSetFactory rsf = new RecoverableSetFactory() {
            public <T> RecoverableSet<T> create(String prefix) {
                return new RecoverableSetImpl<T>(prefix, bindings, info);
            }
        };

        scheduler = new TaskSchedulerImpl(tx, clock, taskContext, rsf);
        scheduler.start();

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

    public class WhenATaskIsBootstrappedInATransaction {

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

    public class WhenARepeatedTaskIsBootstrappedInATransaction {

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

    // TODO: integrate RetryingTaskExecutor: retry limits for repatedly failing tasks
    // TODO: integrate RetryingTaskExecutor: do not retry when the exception is not because of transaction conflict
    // TODO: retry executing tasks in exclusive (single-threaded) mode (ReentrantReadWriteLock?)
}
