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
import static net.orfjackal.dimdwarf.tx.Transaction.Status.*;
import org.jmock.Expectations;
import org.junit.runner.RunWith;

/**
 * @author Esko Luontola
 * @since 15.8.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class TransactionSpec extends Specification<Object> {

    private Transaction tx;
    private TransactionParticipant participant1;
    private TransactionParticipant participant2;

    public void create() throws Exception {
        tx = new Transaction();
        participant1 = mock(TransactionParticipant.class, "participant1");
        participant2 = mock(TransactionParticipant.class, "participant2");
    }

    private static Expectations isNotifiedOnJoin(final TransactionParticipant participant, final Transaction tx) {
        return new Expectations() {{
            one(participant).joinedTransaction(tx);
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
            checking(isNotifiedOnJoin(participant1, tx));
            tx.join(participant1);
            return null;
        }

        public void itHasParticipants() {
            specify(tx.getParticipants(), should.equal(1));
        }

        public void otherParticipantsMayJoinTheSameTransaction() {
            checking(isNotifiedOnJoin(participant2, tx));
            tx.join(participant2);
            specify(tx.getParticipants(), should.equal(2));
        }

        public void theSameParticipantCanNotJoinTwise() {
            tx.join(participant1);
            specify(tx.getParticipants(), should.equal(1));
        }
    }

    public class WhenTransactionPreparesForCommit {

        public Object create() {
            checking(isNotifiedOnJoin(participant1, tx));
            checking(isNotifiedOnJoin(participant2, tx));
            tx.join(participant1);
            tx.join(participant2);
            return null;
        }

        public void allParticipantsAreToldToPrepare() {
            checking(new Expectations() {{
                one(participant1).prepare(tx);
                one(participant2).prepare(tx);
            }});
            tx.prepare();
        }

        public void transactionIsPrepared() {
            allParticipantsAreToldToPrepare();
            specify(tx.getStatus(), should.equal(PREPARE_OK));
        }

        @SuppressWarnings({"ThrowableInstanceNeverThrown"})
        public void prepareFailsIfOneParticipantFailsToPrepare() {
            checking(new Expectations() {{
                one(participant1).prepare(tx);
                one(participant2).prepare(tx); will(throwException(new Throwable("Failed to prepare")));
            }});
            specify(new Block() {
                public void run() throws Throwable {
                    tx.prepare();
                }
            }, should.raise(TransactionFailedException.class));
            specify(tx.getStatus(), should.equal(PREPARE_FAILED));
        }

        public void canNotPrepareTwise() {
            allParticipantsAreToldToPrepare();
            specify(new Block() {
                public void run() throws Throwable {
                    tx.prepare();
                }
            }, should.raise(IllegalStateException.class));
        }

        public void canNotPrepareTwiseConcurrently() {
            WaitOnPrepare blocker = new WaitOnPrepare();
            tx.join(blocker);

            Thread t = new Thread(new Runnable() {
                public void run() {
                    allParticipantsAreToldToPrepare();
                }
            });
            t.setDaemon(true);
            t.start();
            while (t.isAlive() && !blocker.begunPrepare) {
                Thread.yield();
            }

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
    }

    private static class WaitOnPrepare implements TransactionParticipant {

        public volatile boolean begunPrepare = false;

        public void joinedTransaction(Transaction tx) {
        }

        public void prepare(Transaction tx) {
            begunPrepare = true;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // ignore; test has ended
            }
        }
    }
}
