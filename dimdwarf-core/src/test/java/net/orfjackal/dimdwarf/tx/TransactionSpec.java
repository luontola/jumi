// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.tx;

import jdave.*;
import jdave.junit4.JDaveRunner;
import org.jmock.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

import java.util.concurrent.CountDownLatch;

import static net.orfjackal.dimdwarf.tx.TransactionStatus.*;

@RunWith(JDaveRunner.class)
@Group({"fast"})
public class TransactionSpec extends Specification<TransactionContext> {

    private TransactionContext tx;
    private TransactionParticipant participant1;
    private TransactionParticipant participant2;
    private Logger txLogger;

    private CountDownLatch testHasEnded = new CountDownLatch(1);

    public void create() throws Exception {
        txLogger = mock(Logger.class);
        tx = new TransactionContext(txLogger);
        participant1 = mock(TransactionParticipant.class, "participant1");
        participant2 = mock(TransactionParticipant.class, "participant2");
    }

    public void destroy() throws Exception {
        testHasEnded.countDown();
    }

    private void waitUntilTestHasEnded() {
        try {
            testHasEnded.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    private Expectations allParticipantsArePrepared() {
        try {
            return new Expectations() {{
                one(participant1).prepare();
                one(participant2).prepare();
            }};
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    private Expectations prepareFailsFor(final TransactionParticipant participant) {
        try {
            return new Expectations() {{
                one(participant).prepare(); will(throwException(new Throwable("Failed to prepare")));
                allowing(participant1).prepare();
                allowing(participant2).prepare();
            }};
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private Expectations allParticipantsAreCommitted() {
        return new Expectations() {{
            one(participant1).commit();
            one(participant2).commit();
        }};
    }

    private Expectations allParticipantsAreRolledBack() {
        return new Expectations() {{
            one(participant1).rollback();
            one(participant2).rollback();
        }};
    }


    public class WhenTransactionBegins {

        public void itIsActive() {
            specify(tx.getStatus(), should.equal(ACTIVE));
            tx.mustBeActive();
        }

        public void itHasNoParticipants() {
            specify(tx.getParticipants(), should.equal(0));
        }
    }

    public class WhenParticipantJoinsTransaction {

        public void create() {
            tx.join(participant1);
        }

        public void itHasParticipants() {
            specify(tx.getParticipants(), should.equal(1));
        }

        public void otherParticipantsMayJoinTheSameTransaction() {
            tx.join(participant2);
            specify(tx.getParticipants(), should.equal(2));
        }

        public void theSameParticipantCanNotJoinTwise() {
            tx.join(participant1);
            specify(tx.getParticipants(), should.equal(1));
        }
    }

    public class WhenTransactionPreparesToCommit {

        public void create() {
            tx.join(participant1);
            tx.join(participant2);
        }

        public void transactionIsDeactivatedBeforeParticipantsArePrepared() {
            tx.join(new DummyTransactionParticipant() {
                public void prepare() throws Throwable {
                    specify(new Block() {
                        public void run() throws Throwable {
                            tx.mustBeActive();
                        }
                    }, should.raise(TransactionRequiredException.class));
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
            }, should.raise(TransactionRequiredException.class));
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

        public void canNotPrepareTwiseConcurrently() throws InterruptedException {
            checking(allParticipantsArePrepared());

            final CountDownLatch prepareIsInProgress = new CountDownLatch(1);
            tx.join(new DummyTransactionParticipant() {
                public void prepare() {
                    prepareIsInProgress.countDown();
                    waitUntilTestHasEnded();
                }
            });
            new Thread(new Runnable() {
                public void run() {
                    tx.prepare();
                }
            }).start();
            prepareIsInProgress.await();

            specify(tx.getStatus(), should.equal(PREPARING));
            specify(new Block() {
                public void run() throws Throwable {
                    tx.prepare();
                }
            }, should.raise(IllegalStateException.class));
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

        public void create() {
            tx.join(participant1);
            tx.join(participant2);
            checking(allParticipantsArePrepared());
            tx.prepare();
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
                one(participant1).commit(); will(throwException(t)); inSequence(sq);
                one(txLogger).error("Commit failed for participant " + participant1, t); inSequence(sq);
                one(participant2).commit(); inSequence(sq);
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

        public void canNotCommitTwiseConcurrently() throws InterruptedException {
            tx = new TransactionContext(txLogger);

            final CountDownLatch commitIsInProgress = new CountDownLatch(1);
            tx.join(new DummyTransactionParticipant() {
                public void commit() {
                    commitIsInProgress.countDown();
                    waitUntilTestHasEnded();
                }
            });
            new Thread(new Runnable() {
                public void run() {
                    tx.prepare();
                    tx.commit();
                }
            }).start();
            commitIsInProgress.await();

            specify(tx.getStatus(), should.equal(COMMITTING));
            specify(new Block() {
                public void run() throws Throwable {
                    tx.commit();
                }
            }, should.raise(IllegalStateException.class));
        }
    }

    public class RollingBackATransaction {

        public void create() {
            tx.join(participant1);
            tx.join(participant2);
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
                one(participant1).rollback(); will(throwException(t)); inSequence(sq);
                one(txLogger).error("Rollback failed for participant " + participant1, t); inSequence(sq);
                one(participant2).rollback(); inSequence(sq);
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

        public void canNotRollbackTwiseConcurrently() throws InterruptedException {
            checking(allParticipantsAreRolledBack());

            final CountDownLatch rollbackIsInProgress = new CountDownLatch(1);
            tx.join(new DummyTransactionParticipant() {
                public void rollback() {
                    rollbackIsInProgress.countDown();
                    waitUntilTestHasEnded();
                }
            });
            new Thread(new Runnable() {
                public void run() {
                    tx.rollback();
                }
            }).start();
            rollbackIsInProgress.await();

            specify(tx.getStatus(), should.equal(ROLLING_BACK));
            specify(new Block() {
                public void run() throws Throwable {
                    tx.rollback();
                }
            }, should.raise(IllegalStateException.class));
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
                public void prepare() throws Throwable {
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
            }, should.raise(TransactionRolledbackException.class));
            specify(tx.getStatus(), should.equal(PREPARED));
        }
    }


    private static class DummyTransactionParticipant implements TransactionParticipant {

        public void prepare() throws Throwable {
        }

        public void commit() {
        }

        public void rollback() {
        }
    }
}
