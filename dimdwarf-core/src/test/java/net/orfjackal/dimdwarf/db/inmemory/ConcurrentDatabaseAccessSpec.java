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

package net.orfjackal.dimdwarf.db.inmemory;

import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.db.*;
import static net.orfjackal.dimdwarf.db.Blob.EMPTY_BLOB;
import net.orfjackal.dimdwarf.tx.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

/**
 * @author Esko Luontola
 * @since 18.8.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class ConcurrentDatabaseAccessSpec extends Specification<Object> {

    private static final String TABLE = "test";

    private InMemoryDatabase dbms;
    private TransactionCoordinator tx1;
    private TransactionCoordinator tx2;
    private DatabaseTable<Blob, Blob> table1;
    private DatabaseTable<Blob, Blob> table2;
    private Logger txLogger;

    private Blob key;
    private Blob value1;
    private Blob value2;
    private Blob value3;

    public void create() throws Exception {
        dbms = new InMemoryDatabase();
        txLogger = mock(Logger.class);
        tx1 = new TransactionImpl(txLogger);
        tx2 = new TransactionImpl(txLogger);

        key = Blob.fromBytes(new byte[]{0});
        value1 = Blob.fromBytes(new byte[]{1});
        value2 = Blob.fromBytes(new byte[]{2});
        value3 = Blob.fromBytes(new byte[]{3});
        specify(dbms.getOpenConnections(), should.equal(0));
    }

    private Blob readInNewTransaction(Blob key) {
        TransactionCoordinator tx = new TransactionImpl(txLogger);
        try {
            return dbms.openConnection(tx.getTransaction()).openTable(TABLE).read(key);
        } finally {
            tx.prepareAndCommit();
        }
    }

    private void updateInNewTransaction(Blob key, Blob value) {
        TransactionCoordinator tx = new TransactionImpl(txLogger);
        dbms.openConnection(tx.getTransaction()).openTable(TABLE).update(key, value);
        tx.prepareAndCommit();
    }

    private void tx1PreparesBeforeTx2() {
        tx1.prepare();
        specify(new Block() {
            public void run() throws Throwable {
                tx2.prepare();
            }
        }, should.raise(TransactionException.class));
        tx1.commit();
        tx2.rollback();
    }

    private void tx1PreparesAndCommitsBeforeTx2() {
        tx1.prepare();
        tx1.commit();
        specify(new Block() {
            public void run() throws Throwable {
                tx2.prepare();
            }
        }, should.raise(TransactionException.class));
        tx2.rollback();
    }

    public class WhenMultipleDatabaseConnectionsAreOpen {

        public void create() {
            table1 = dbms.openConnection(tx1.getTransaction()).openTable(TABLE);
            table2 = dbms.openConnection(tx2.getTransaction()).openTable(TABLE);
        }

        public void databaseKeepsTrackOfTheNumberOfOpenConnections() {
            specify(dbms.getOpenConnections(), should.equal(2));
            tx1.prepareAndCommit();
            specify(dbms.getOpenConnections(), should.equal(1));
            tx2.prepareAndCommit();
            specify(dbms.getOpenConnections(), should.equal(0));
        }

        public void databaseKeepsTrackOfTheCurrentRevision() {
            specify(dbms.getCurrentRevision(), should.equal(0));
            tx1.prepareAndCommit();
            specify(dbms.getCurrentRevision(), should.equal(1));
            tx2.prepareAndCommit();
            specify(dbms.getCurrentRevision(), should.equal(2));
        }

        public void databaseKeepsTrackOfTheOldestUncommittedRevision() {
            specify(dbms.getOldestUncommittedRevision(), should.equal(0));
            tx1.prepareAndCommit();
            specify(dbms.getOldestUncommittedRevision(), should.equal(0));
            tx2.prepareAndCommit();
            specify(dbms.getOldestUncommittedRevision(), should.equal(2));
        }
    }

    public class WhenEntryIsCreatedInATransaction {

        public void create() {
            table1 = dbms.openConnection(tx1.getTransaction()).openTable(TABLE);
            table2 = dbms.openConnection(tx2.getTransaction()).openTable(TABLE);
            table1.update(key, value1);
            specify(dbms.getOpenConnections(), should.equal(2));
        }

        public void otherTransactionsCanNotSeeIt() {
            specify(table2.read(key), should.equal(EMPTY_BLOB));
        }

        public void afterCommitNewTransactionsCanSeeIt() {
            tx1.prepareAndCommit();
            specify(readInNewTransaction(key), should.equal(value1));
        }

        public void afterCommitOldTransactionsStillCanNotSeeIt() {
            tx1.prepareAndCommit();
            specify(table2.read(key), should.equal(EMPTY_BLOB));
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

        public void create() {
            updateInNewTransaction(key, value1);
            table1 = dbms.openConnection(tx1.getTransaction()).openTable(TABLE);
            table2 = dbms.openConnection(tx2.getTransaction()).openTable(TABLE);
            table1.update(key, value2);
            specify(dbms.getOpenConnections(), should.equal(2));
        }

        public void otherTransactionsCanNotSeeIt() {
            specify(table2.read(key), should.equal(value1));
        }

        public void afterCommitNewTransactionsCanSeeIt() {
            tx1.prepareAndCommit();
            specify(readInNewTransaction(key), should.equal(value2));
        }

        public void afterCommitOldTransactionsStillCanNotSeeIt() {
            tx1.prepareAndCommit();
            specify(table2.read(key), should.equal(value1));
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

        public void create() {
            updateInNewTransaction(key, value1);
            table1 = dbms.openConnection(tx1.getTransaction()).openTable(TABLE);
            table2 = dbms.openConnection(tx2.getTransaction()).openTable(TABLE);
            table1.delete(key);
            specify(dbms.getOpenConnections(), should.equal(2));
        }


        public void otherTransactionsCanNotSeeIt() {
            specify(table2.read(key), should.equal(value1));
        }

        public void afterCommitNewTransactionsCanSeeIt() {
            tx1.prepareAndCommit();
            specify(readInNewTransaction(key), should.equal(EMPTY_BLOB));
        }

        public void afterCommitOldTransactionsStillCanNotSeeIt() {
            tx1.prepareAndCommit();
            specify(table2.read(key), should.equal(value1));
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

        public void create() {
            table1 = dbms.openConnection(tx1.getTransaction()).openTable(TABLE);
            table2 = dbms.openConnection(tx2.getTransaction()).openTable(TABLE);
            table1.update(key, value1);
            table2.update(key, value2);
        }

        public void onlyTheFirstToPrepareWillSucceed() {
            tx1PreparesBeforeTx2();
            specify(readInNewTransaction(key), should.equal(value1));
        }

        public void onlyTheFirstToPrepareAndCommitWillSucceed() {
            tx1PreparesAndCommitsBeforeTx2();
            specify(readInNewTransaction(key), should.equal(value1));
        }
    }

    public class IfTwoTransactionsUpdateAnEntryWithTheSameKey {

        public void create() {
            updateInNewTransaction(key, value3);
            table1 = dbms.openConnection(tx1.getTransaction()).openTable(TABLE);
            table2 = dbms.openConnection(tx2.getTransaction()).openTable(TABLE);
            table1.update(key, value1);
            table2.update(key, value2);
        }

        public void onlyTheFirstToPrepareWillSucceed() {
            tx1PreparesBeforeTx2();
            specify(readInNewTransaction(key), should.equal(value1));
        }

        public void onlyTheFirstToPrepareAndCommitWillSucceed() {
            tx1PreparesAndCommitsBeforeTx2();
            specify(readInNewTransaction(key), should.equal(value1));
        }

        public void theKeyMayBeUpdatedInALaterTransaction() {
            // Checks that InMemoryDatabaseTable releases its commit locks if there is a modification conflict.
            tx1PreparesAndCommitsBeforeTx2();
            updateInNewTransaction(key, value2);
            specify(readInNewTransaction(key), should.equal(value2));
        }
    }

    public class IfTwoTransactionsDeleteAnEntryWithTheSameKey {

        public void create() {
            updateInNewTransaction(key, value3);
            table1 = dbms.openConnection(tx1.getTransaction()).openTable(TABLE);
            table2 = dbms.openConnection(tx2.getTransaction()).openTable(TABLE);
            table1.delete(key);
            table2.delete(key);
        }

        public void onlyTheFirstToPrepareWillSucceed() {
            tx1PreparesBeforeTx2();
            specify(readInNewTransaction(key), should.equal(Blob.EMPTY_BLOB));
        }

        public void onlyTheFirstToPrepareAndCommitWillSucceed() {
            tx1PreparesAndCommitsBeforeTx2();
            specify(readInNewTransaction(key), should.equal(Blob.EMPTY_BLOB));
        }
    }

    // TODO: provide a SortedMap interface to the database?
}
