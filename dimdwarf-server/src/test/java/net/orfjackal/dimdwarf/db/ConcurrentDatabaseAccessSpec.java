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

package net.orfjackal.dimdwarf.db;

import jdave.Block;
import jdave.Group;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import static net.orfjackal.dimdwarf.db.Blob.EMPTY_BLOB;
import net.orfjackal.dimdwarf.tx.TransactionCoordinator;
import net.orfjackal.dimdwarf.tx.TransactionFailedException;
import net.orfjackal.dimdwarf.tx.TransactionImpl;
import org.junit.runner.RunWith;

/**
 * @author Esko Luontola
 * @since 18.8.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class ConcurrentDatabaseAccessSpec extends Specification<Object> {

    private InMemoryDatabase db;
    private TransactionCoordinator tx1;
    private TransactionCoordinator tx2;
    private DatabaseConnection db1;
    private DatabaseConnection db2;

    private Blob key;
    private Blob value1;
    private Blob value2;

    public void create() throws Exception {
        db = new InMemoryDatabase();
        tx1 = new TransactionImpl();
        tx2 = new TransactionImpl();

        key = Blob.fromBytes(new byte[]{0});
        value1 = Blob.fromBytes(new byte[]{1});
        value2 = Blob.fromBytes(new byte[]{2});
        specify(db.openConnections(), should.equal(0));
    }

    private Blob readInNewTransaction(Blob key) {
        TransactionCoordinator tx = new TransactionImpl();
        try {
            return db.openConnection(tx.getTransaction())
                    .read(key);
        } finally {
            tx.prepare();
            tx.commit();
        }
    }

    private void updateInNewTransaction(Blob key, Blob value) {
        TransactionCoordinator tx = new TransactionImpl();
        db.openConnection(tx.getTransaction())
                .update(key, value);
        tx.prepare();
        tx.commit();
    }


    public class WhenEntryIsCreatedInATransaction {

        public Object create() {
            db1 = db.openConnection(tx1.getTransaction());
            db2 = db.openConnection(tx2.getTransaction());
            db1.update(key, value1);
            specify(db.openConnections(), should.equal(2));
            return null;
        }

        public void otherTransactionsCanNotSeeIt() {
            specify(db2.read(key), should.equal(EMPTY_BLOB));
        }

        public void afterCommitNewTransactionsCanSeeIt() {
            tx1.prepare();
            tx1.commit();
            specify(readInNewTransaction(key), should.equal(value1));
        }

        public void afterCommitOldTransactionsStillCanNotSeeIt() {
            tx1.prepare();
            tx1.commit();
            specify(db2.read(key), should.equal(EMPTY_BLOB));
        }

        public void onRollbackTheModificationsAreDiscarded() {
            tx1.rollback();
            specify(readInNewTransaction(key), should.equal(EMPTY_BLOB));
        }

        public void onPrepareAndRollbackTheLocksAreReleased() {
            tx1.prepare();
            tx1.rollback();
            specify(readInNewTransaction(key), should.equal(EMPTY_BLOB));
            updateInNewTransaction(key, value2);
            specify(readInNewTransaction(key), should.equal(value2));
        }
    }

    public class WhenEntryIsUpdatedInATransaction {

        public Object create() {
            updateInNewTransaction(key, value1);
            db1 = db.openConnection(tx1.getTransaction());
            db2 = db.openConnection(tx2.getTransaction());
            db1.update(key, value2);
            specify(db.openConnections(), should.equal(2));
            return null;
        }

        public void otherTransactionsCanNotSeeIt() {
            specify(db2.read(key), should.equal(value1));
        }

        public void afterCommitNewTransactionsCanSeeIt() {
            tx1.prepare();
            tx1.commit();
            specify(readInNewTransaction(key), should.equal(value2));
        }

        public void afterCommitOldTransactionsStillCanNotSeeIt() {
            tx1.prepare();
            tx1.commit();
            specify(db2.read(key), should.equal(value1));
        }

        public void onRollbackTheModificationsAreDiscarded() {
            tx1.rollback();
            specify(readInNewTransaction(key), should.equal(value1));
        }

        public void onPrepareAndRollbackTheLocksAreReleased() {
            tx1.prepare();
            tx1.rollback();
            specify(readInNewTransaction(key), should.equal(value1));
            updateInNewTransaction(key, value2);
            specify(readInNewTransaction(key), should.equal(value2));
        }
    }

    public class WhenEntryIsDeletedInATransaction {

        public Object create() {
            updateInNewTransaction(key, value1);
            db1 = db.openConnection(tx1.getTransaction());
            db2 = db.openConnection(tx2.getTransaction());
            db1.delete(key);
            specify(db.openConnections(), should.equal(2));
            return null;
        }


        public void otherTransactionsCanNotSeeIt() {
            specify(db2.read(key), should.equal(value1));
        }

        public void afterCommitNewTransactionsCanSeeIt() {
            tx1.prepare();
            tx1.commit();
            specify(readInNewTransaction(key), should.equal(EMPTY_BLOB));
        }

        public void afterCommitOldTransactionsStillCanNotSeeIt() {
            tx1.prepare();
            tx1.commit();
            specify(db2.read(key), should.equal(value1));
        }

        public void onRollbackTheModificationsAreDiscarded() {
            tx1.rollback();
            specify(readInNewTransaction(key), should.equal(value1));
        }

        public void onPrepareAndRollbackTheLocksAreReleased() {
            tx1.prepare();
            tx1.rollback();
            specify(readInNewTransaction(key), should.equal(value1));
            updateInNewTransaction(key, value2);
            specify(readInNewTransaction(key), should.equal(value2));
        }
    }

    public class IfTwoTransactionsCreateAnEntryWithTheSameKey {

        public Object create() {
            db1 = db.openConnection(tx1.getTransaction());
            db2 = db.openConnection(tx2.getTransaction());
            db1.update(key, value1);
            db2.update(key, value2);
            return null;
        }

        public void onlyTheFirstToCommitWillSucceed() {
            tx1.prepare();
            tx1.commit();
            specify(new Block() {
                public void run() throws Throwable {
                    tx2.prepare();
                }
            }, should.raise(TransactionFailedException.class));
            tx2.rollback();
            specify(readInNewTransaction(key), should.equal(value1));
        }

        public void onlyTheFirstToPrepareWillSucceed() {
            tx1.prepare();
            specify(new Block() {
                public void run() throws Throwable {
                    tx2.prepare();
                }
            }, should.raise(TransactionFailedException.class));
            tx1.commit();
            tx2.rollback();
            specify(readInNewTransaction(key), should.equal(value1));
        }
    }

    // TODO: conflict in update
    // TODO: conflict in delete
    // TODO: allow only one transaction to prepare at a time (?), no conflicts are allowed on commit
    // TODO: multiple tables per database
    // TODO: iterator (for tables and entries), used for writing all entries to disk

    // TODO: String and BigInteger wrappers for keys
    // TODO: auto-increment keys (BigInteger), create should not conflict when using them
    // TODO: find all keys (String) starting with a string
}
