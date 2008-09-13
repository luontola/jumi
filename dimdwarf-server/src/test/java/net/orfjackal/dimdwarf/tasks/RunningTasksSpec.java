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

package net.orfjackal.dimdwarf.tasks;

import com.google.inject.Guice;
import com.google.inject.Injector;
import jdave.Block;
import jdave.Group;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.context.ThreadContext;
import net.orfjackal.dimdwarf.modules.TaskScopeModule;
import net.orfjackal.dimdwarf.tx.Transaction;
import net.orfjackal.dimdwarf.tx.TransactionException;
import net.orfjackal.dimdwarf.tx.TransactionParticipant;
import net.orfjackal.dimdwarf.tx.TransactionStatus;
import org.jmock.Expectations;
import org.junit.runner.RunWith;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Esko Luontola
 * @since 13.9.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class RunningTasksSpec extends Specification<Object> {

    private Injector injector;
    private TaskExecutor taskExecutor;
    private Logger logger;

    public void create() throws Exception {
        injector = Guice.createInjector(new TaskScopeModule());
        taskExecutor = injector.getInstance(TaskExecutor.class);
        logger = Logger.getLogger(TaskExecutor.class.getName());
        logger.setLevel(Level.OFF);
    }

    public void destroy() throws Exception {
        logger.setLevel(Level.ALL);
        if (ThreadContext.currentContext() != null) {
            ThreadContext.tearDown();
        }
    }

    private Transaction getTransaction() {
        return injector.getInstance(Transaction.class);
    }


    public class WhenATaskIsExecuted {

        public Object create() {
            return null;
        }

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
                            one(participant).prepare(tx);
                            one(participant).commit(tx);
                        }});
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                    tx.join(participant);
                }
            });
        }

        public void theTransactionIsRolledBackIfAnExceptionIsRaised() {
            final Runnable exceptionInTask = new Runnable() {
                public void run() {
                    final TransactionParticipant participant = mock(TransactionParticipant.class);
                    final Transaction tx = getTransaction();
                    checking(new Expectations() {{
                        one(participant).rollback(tx);
                    }});
                    tx.join(participant);
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
                    final TransactionParticipant participant = mock(TransactionParticipant.class);
                    final Transaction tx = getTransaction();
                    try {
                        checking(new Expectations() {{
                            one(participant).prepare(tx); will(throwException(new RuntimeException("dummy exception")));
                            one(participant).rollback(tx);
                        }});
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                    tx.join(participant);
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
