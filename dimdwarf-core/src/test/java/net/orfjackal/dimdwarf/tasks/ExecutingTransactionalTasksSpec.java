// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.tasks;

import com.google.inject.*;
import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.modules.*;
import net.orfjackal.dimdwarf.tx.*;
import org.jmock.Expectations;
import org.junit.runner.RunWith;

import java.util.concurrent.Executor;
import java.util.logging.*;

@RunWith(JDaveRunner.class)
@Group({"fast"})
public class ExecutingTransactionalTasksSpec extends Specification<Object> {

    private Injector injector;
    private Executor taskExecutor;
    private Logger hideTransactionFailedLogs;

    public void create() throws Exception {
        injector = Guice.createInjector(
                new TaskContextModule(),
                new FakeEntityModule(this)
        );
        taskExecutor = injector.getInstance(TaskExecutor.class);

        hideTransactionFailedLogs = Logger.getLogger(TransactionFilter.class.getName());
        hideTransactionFailedLogs.setLevel(Level.SEVERE);
    }

    public void destroy() throws Exception {
        hideTransactionFailedLogs.setLevel(Level.ALL);
    }

    private Transaction getTransaction() {
        return injector.getInstance(Transaction.class);
    }


    public class WhenATaskIsExecuted {

        public void aTransactionIsActive() {
            taskExecutor.execute(new Runnable() {
                public void run() {
                    Transaction tx = getTransaction();
                    specify(tx.getStatus(), should.equal(TransactionStatus.ACTIVE));
                }
            });
        }

        public void theTransactionIsCommittedWhenTheTaskEnds() {
            taskExecutor.execute(new Runnable() {
                public void run() {
                    final TransactionParticipant participant = mock(TransactionParticipant.class);
                    final Transaction tx = getTransaction();
                    try {
                        checking(new Expectations() {{
                            one(participant).prepare();
                            one(participant).commit();
                        }});
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                    tx.join(participant);
                }
            });
        }

        public void theTransactionIsRolledBackIfAnExceptionIsRaisedDuringTaskExecution() {
            final Runnable exceptionInTask = new Runnable() {
                public void run() {
                    final TransactionParticipant txSpy = mock(TransactionParticipant.class);
                    final Transaction tx = getTransaction();
                    checking(new Expectations() {{
                        one(txSpy).rollback();
                    }});
                    tx.join(txSpy);
                    throw new IllegalArgumentException("dummy exception");
                }
            };
            specify(new Block() {
                public void run() throws Throwable {
                    taskExecutor.execute(exceptionInTask);
                }
            }, should.raise(IllegalArgumentException.class));
        }

        public void theTransactionIsRolledBackIfAnExceptionIsRaisedDuringPrepare() {
            final Runnable exceptionInPrepare = new Runnable() {
                @SuppressWarnings({"ThrowableInstanceNeverThrown"})
                public void run() {
                    final TransactionParticipant exceptionThrower = mock(TransactionParticipant.class);
                    final Transaction tx = getTransaction();
                    try {
                        checking(new Expectations() {{
                            one(exceptionThrower).prepare(); will(throwException(new IllegalArgumentException("dummy exception")));
                            one(exceptionThrower).rollback();
                        }});
                    } catch (Throwable t) {
                        throw new AssertionError(t);
                    }
                    tx.join(exceptionThrower);
                }
            };
            specify(new Block() {
                public void run() throws Throwable {
                    taskExecutor.execute(exceptionInPrepare);
                }
            }, should.raise(TransactionException.class));
        }
    }
}
