/*
 * Copyright (c) 2008, Esko Luontola. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
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

package net.orfjackal.dimdwarf.tx;

import jdave.Block;
import jdave.Group;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import static net.orfjackal.dimdwarf.tx.TransactionStatus.*;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.junit.runner.RunWith;

import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @author Esko Luontola
 * @since 15.8.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class TransactionSpec extends Specification<TransactionImpl> {

    private TransactionImpl tx;
    private TransactionParticipant participant1;
    private TransactionParticipant participant2;

    private Logger txLogger;
    private Filter txLoggerFilter;

    public void create() throws Exception {
        txLoggerFilter = mock(Filter.class);
        txLogger = Logger.getLogger(TransactionImpl.class.getName());
        txLogger.setFilter(txLoggerFilter);

        tx = new TransactionImpl();
        participant1 = mock(TransactionParticipant.class, "participant1");
        participant2 = mock(TransactionParticipant.class, "participant2");
    }

    public void destroy() throws Exception {
        txLogger.setFilter(null);
    }

    private Expectations isNotifiedOnJoin(final TransactionParticipant participant) {
        return new Expectations() {{
            one(participant).joinedTransaction(tx);
        }};
    }

    private Expectations allParticipantsArePrepared() {
        try {
            return new Expectations() {{
                one(participant1).prepare(tx);
                one(participant2).prepare(tx);
            }};
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    private Expectations prepareFailsFor(final TransactionParticipant participant) {
        try {
            return new Expectations() {{
                one(participant).prepare(tx); will(throwException(new Throwable("Failed to prepare")));
                allowing(participant1).prepare(tx);
                allowing(participant2).prepare(tx);
            }};
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private Expectations allParticipantsAreCommitted() {
        return new Expectations() {{
            one(participant1).commit(tx);
            one(participant2).commit(tx);
        }};
    }

    private Expectations allParticipantsAreRolledBack() {
        return new Expectations() {{
            one(participant1).rollback(tx);
            one(participant2).rollback(tx);
        }};
    }


    public class WhenTransactionBegins {

        public Object create() {
            return null;
        }

        public void itIsActive() {
            specify(tx.getStatus(), should.equal(ACTIVE));
            tx.mustBeActive();
        }

        public void itHasNoParticipants() {
            specify(tx.getParticipants(), should.equal(0));
        }
    }

    public class WhenParticipantJoinsTransaction {

        public Object create() {
            checking(isNotifiedOnJoin(participant1));
            tx.join(participant1);
            return null;
        }

        public void itHasParticipants() {
            specify(tx.getParticipants(), should.equal(1));
        }

        public void otherParticipantsMayJoinTheSameTransaction() {
            checking(isNotifiedOnJoin(participant2));
            tx.join(participant2);
            specify(tx.getParticipants(), should.equal(2));
        }

        public void theSameParticipantCanNotJoinTwise() {
            tx.join(participant1);
            specify(tx.getParticipants(), should.equal(1));
        }
    }

    public class WhenTransactionPreparesToCommit {

        public Object create() {
            checking(isNotifiedOnJoin(participant1));
            checking(isNotifiedOnJoin(participant2));
            tx.join(participant1);
            tx.join(participant2);
            return null;
        }

        public void transactionIsDeactivatedBeforeParticipantsArePrepared() {
            tx.join(new DummyTransactionParticipant() {
                public void prepare(final Transaction tx) throws Throwable {
                    specify(new Block() {
                        public void run() throws Throwable {
                            tx.mustBeActive();
                        }
                    }, should.raise(IllegalStateException.class));
                }
            });
            checking(allParticipantsArePrepared());
            tx.prepare();
        }

        public void transactionMayNotBeJoinedAfterItIsDeactivated() {
            checking(allParticipantsArePrepared());
            tx.prepare();
            specify(new Block() {
                public void run() throws Throwable {
                    tx.join(new DummyTransactionParticipant());
                }
            }, should.raise(IllegalStateException.class));
        }

        public void allParticipantsAreToldToPrepare() {
            checking(allParticipantsArePrepared());
            tx.prepare();
        }

        public void transactionIsPrepared() {
            checking(allParticipantsArePrepared());
            tx.prepare();
            specify(tx.getStatus(), should.equal(PREPARED));
        }

        public void prepareFailsIfOneParticipantFailsToPrepare() {
            checking(prepareFailsFor(participant1));
            specify(new Block() {
                public void run() throws Throwable {
                    tx.prepare();
                }
            }, should.raise(TransactionException.class));
            specify(tx.getStatus(), should.equal(PREPARE_FAILED));
        }

        public void canNotPrepareTwise() {
            checking(allParticipantsArePrepared());
            tx.prepare();
            specify(new Block() {
                public void run() throws Throwable {
                    tx.prepare();
                }
            }, should.raise(IllegalStateException.class));
        }

        public void canNotPrepareTwiseConcurrently() {
            checking(allParticipantsArePrepared());
            Blocker blocker = new Blocker() {
                public void prepare(Transaction tx) {
                    waitHere();
                }
            };
            tx.join(blocker);

            Thread t = blocker.waitUntilBlockerBeginsWaiting(new Runnable() {
                public void run() {
                    tx.prepare();
                }
            });

            specify(tx.getStatus(), should.equal(PREPARING));
            specify(new Block() {
                public void run() throws Throwable {
                    tx.prepare();
                }
            }, should.raise(IllegalStateException.class));
            t.interrupt();
        }

        public void canNotCommitBeforePrepare() {
            specify(new Block() {
                public void run() throws Throwable {
                    tx.commit();
                }
            }, should.raise(IllegalStateException.class));
        }

        public void canNotCommitIfPrepareFailed() {
            checking(prepareFailsFor(participant2));
            specify(new Block() {
                public void run() throws Throwable {
                    tx.prepare();
                }
            }, should.raise(TransactionException.class));
            specify(new Block() {
                public void run() throws Throwable {
                    tx.commit();
                }
            }, should.raise(IllegalStateException.class));
        }
    }

    public class WhenTransactionCommits {

        public Object create() {
            checking(isNotifiedOnJoin(participant1));
            checking(isNotifiedOnJoin(participant2));
            tx.join(participant1);
            tx.join(participant2);
            checking(allParticipantsArePrepared());
            tx.prepare();
            return null;
        }

        public void allParticipantsAreToldToCommit() {
            checking(allParticipantsAreCommitted());
            tx.commit();
        }

        public void transactionIsCommitted() {
            checking(allParticipantsAreCommitted());
            tx.commit();
            specify(tx.getStatus(), should.equal(COMMITTED));
        }

        @SuppressWarnings({"ThrowableInstanceNeverThrown"})
        public void theRestOfTheParticipantsAreCommittedEvenIfTheFirstParticipantFailsToCommit() {
            final Sequence sq = sequence("commit-sequence");
            final Throwable t = new AssertionError("Failed to commit");
            checking(new Expectations() {{
                one(participant1).commit(tx); will(throwException(t)); inSequence(sq);
                one(txLoggerFilter).isLoggable(with(any(LogRecord.class))); inSequence(sq);
                one(participant2).commit(tx); inSequence(sq);
            }});
            tx.commit();
            specify(tx.getStatus(), should.equal(COMMITTED));
        }

        public void canNotCommitTwise() {
            checking(allParticipantsAreCommitted());
            tx.commit();
            specify(new Block() {
                public void run() throws Throwable {
                    tx.commit();
                }
            }, should.raise(IllegalStateException.class));
        }

        public void canNotCommitTwiseConcurrently() {
            tx = new TransactionImpl();
            Blocker blocker = new Blocker() {
                public void commit(Transaction tx) {
                    waitHere();
                }
            };
            tx.join(blocker);
            tx.prepare();

            Thread t = blocker.waitUntilBlockerBeginsWaiting(new Runnable() {
                public void run() {
                    tx.commit();
                }
            });

            specify(tx.getStatus(), should.equal(COMMITTING));
            specify(new Block() {
                public void run() throws Throwable {
                    tx.commit();
                }
            }, should.raise(IllegalStateException.class));
            t.interrupt();
        }
    }

    public class RollingBackATransaction {

        public Object create() {
            checking(isNotifiedOnJoin(participant1));
            checking(isNotifiedOnJoin(participant2));
            tx.join(participant1);
            tx.join(participant2);
            return null;
        }

        public void allParticipantsAreToldToRollBack() {
            checking(allParticipantsAreRolledBack());
            tx.rollback();
        }

        public void transactionIsRolledBack() {
            checking(allParticipantsAreRolledBack());
            tx.rollback();
            specify(tx.getStatus(), should.equal(ROLLED_BACK));
        }

        @SuppressWarnings({"ThrowableInstanceNeverThrown"})
        public void theRestOfTheParticipantsAreRolledBackEvenIfTheFirstParticipantFailsToRollBack() {
            final Sequence sq = sequence("rollback-sequence");
            final Throwable t = new AssertionError("Failed to rollback");
            checking(new Expectations() {{
                one(participant1).rollback(tx); will(throwException(t)); inSequence(sq);
                one(txLoggerFilter).isLoggable(with(any(LogRecord.class))); inSequence(sq);
                one(participant2).rollback(tx); inSequence(sq);
            }});
            tx.rollback();
            specify(tx.getStatus(), should.equal(ROLLED_BACK));
        }

        public void canNotRollbackTwise() {
            checking(allParticipantsAreRolledBack());
            tx.rollback();
            specify(new Block() {
                public void run() throws Throwable {
                    tx.rollback();
                }
            }, should.raise(IllegalStateException.class));
        }

        public void canNotRollbackTwiseConcurrently() {
            checking(allParticipantsAreRolledBack());
            Blocker blocker = new Blocker() {
                public void rollback(Transaction tx) {
                    waitHere();
                }
            };
            tx.join(blocker);

            Thread t = blocker.waitUntilBlockerBeginsWaiting(new Runnable() {
                public void run() {
                    tx.rollback();
                }
            });

            specify(tx.getStatus(), should.equal(ROLLING_BACK));
            specify(new Block() {
                public void run() throws Throwable {
                    tx.rollback();
                }
            }, should.raise(IllegalStateException.class));
            t.interrupt();
        }

        public void mayRollbackWhenActive() {
            checking(allParticipantsAreRolledBack());
            tx.rollback();
        }

        public void mayRollbackWhenPrepared() {
            checking(allParticipantsArePrepared());
            tx.prepare();
            checking(allParticipantsAreRolledBack());
            tx.rollback();
        }

        public void mayRollbackWhenPrepareFailed() {
            checking(prepareFailsFor(participant1));
            specify(new Block() {
                public void run() throws Throwable {
                    tx.prepare();
                }
            }, should.raise(TransactionException.class));
            checking(allParticipantsAreRolledBack());
            tx.rollback();
        }

        public void canNotRollbackWhenCommitted() {
            checking(allParticipantsArePrepared());
            tx.prepare();
            checking(allParticipantsAreCommitted());
            tx.commit();
            specify(new Block() {
                public void run() throws Throwable {
                    tx.rollback();
                }
            }, should.raise(IllegalStateException.class));
        }
    }

    public class MarkingATransactionForRollbackOnly {

        public Object create() {
            return null;
        }

        public void atFirstItIsNotRollbackOnly() {
            specify(!tx.isRollbackOnly());
        }

        public void afterMarkingItIsRollbackOnly() {
            tx.setRollbackOnly();
            specify(tx.isRollbackOnly());
        }

        public void prepareFailsWhenItIsRollbackOnly() {
            tx.setRollbackOnly();
            specify(tx.getStatus(), should.equal(ACTIVE));
            specify(new Block() {
                public void run() throws Throwable {
                    tx.prepare();
                }
            }, should.raise(TransactionException.class));
            specify(tx.getStatus(), should.equal(PREPARE_FAILED));
        }

        public void prepareFailsWhenItIsMarkedRollbackOnlyDuringPrepare() {
            tx.join(new DummyTransactionParticipant() {
                public void prepare(Transaction tx) throws Throwable {
                    tx.setRollbackOnly();
                }
            });
            specify(tx.getStatus(), should.equal(ACTIVE));
            specify(new Block() {
                public void run() throws Throwable {
                    tx.prepare();
                }
            }, should.raise(TransactionException.class));
            specify(tx.getStatus(), should.equal(PREPARE_FAILED));
        }

        public void commitFailsWhenItIsRollbackOnly() {
            tx.prepare();
            tx.setRollbackOnly();
            specify(tx.getStatus(), should.equal(PREPARED));
            specify(new Block() {
                public void run() throws Throwable {
                    tx.commit();
                }
            }, should.raise(IllegalStateException.class));
            specify(tx.getStatus(), should.equal(PREPARED));
        }
    }


    private static class Blocker extends DummyTransactionParticipant {

        private volatile boolean isReached = false;

        protected void waitHere() {
            isReached = true;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // ignore; test has ended
            }
        }

        public Thread waitUntilBlockerBeginsWaiting(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.start();
            while (t.isAlive() && !isReached) {
                Thread.yield();
            }
            return t;
        }
    }

    private static class DummyTransactionParticipant implements TransactionParticipant {

        public void joinedTransaction(Transaction tx) {
        }

        public void prepare(Transaction tx) throws Throwable {
        }

        public void commit(Transaction tx) {
        }

        public void rollback(Transaction tx) {
        }
    }
}
