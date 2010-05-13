// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db.inmemory;

import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.db.*;
import net.orfjackal.dimdwarf.tx.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

@RunWith(JDaveRunner.class)
@Group({"fast"})
public class MultipleDatabaseTablesSpec extends Specification<Object> {

    private static final String TABLE1 = "table1";
    private static final String TABLE2 = "table2";

    private InMemoryDatabaseManager dbms;
    private TransactionCoordinator tx;
    private Database<Blob, Blob> db;
    private DatabaseTable<Blob, Blob> table1;
    private DatabaseTable<Blob, Blob> table2;
    private Logger txLogger;

    private Blob key = Blob.fromBytes(new byte[]{0});
    private Blob value1 = Blob.fromBytes(new byte[]{1});
    private Blob value2 = Blob.fromBytes(new byte[]{2});
    private Blob value3 = Blob.fromBytes(new byte[]{3});

    public void create() throws Exception {
        dbms = new InMemoryDatabaseManager();
        txLogger = mock(Logger.class);
        tx = new TransactionContext(txLogger);
        db = dbms.openConnection(tx.getTransaction());
        table1 = db.openTable(TABLE1);
        table2 = db.openTable(TABLE2);
    }

    private Blob readInNewTransaction(String table, Blob key) {
        TransactionCoordinator tx = new TransactionContext(txLogger);
        try {
            return dbms.openConnection(tx.getTransaction()).openTable(table).read(key);
        } finally {
            tx.prepareAndCommit();
        }
    }

    private void updateInNewTransaction(String table, Blob key, Blob value) {
        TransactionCoordinator tx = new TransactionContext(txLogger);
        try {
            dbms.openConnection(tx.getTransaction()).openTable(table).update(key, value);
        } finally {
            tx.prepareAndCommit();
        }
    }


    public class OpeningDatabaseTables {

        public void eachTableHasAName() {
            specify(db.getTableNames(), should.containExactly(TABLE1, TABLE2));
        }

        public void theSameNamesWillCorrespondTheSameTable() {
            specify(db.openTable(TABLE1), should.equal(table1));
        }

        public void differentNamesWillCorrespondDifferentTables() {
            specify(table1, should.not().equal(table2));
        }

        public void openingATableWhichDoesNotExistWillCreateThatTable() {
            specify(db.openTable("newTable"), should.not().equal(null));
            specify(db.getTableNames(), should.containExactly(TABLE1, TABLE2, "newTable"));
        }
    }

    public class DuringTransaction {

        public void create() {
            table1.update(key, value1);
        }

        public void updatesAreSeenInTheUpdatedTable() {
            specify(table1.read(key), should.equal(value1));
        }

        public void updatesAreNotSeenInOtherTables() {
            specify(table2.read(key), should.equal(Blob.EMPTY_BLOB));
        }

        public void updatesAreNotSeenInOtherTransactions() {
            specify(readInNewTransaction(TABLE1, key), should.equal(Blob.EMPTY_BLOB));
        }
    }

    public class AfterTransactionIsCommitted {

        public void create() {
            table1.update(key, value1);
            tx.prepareAndCommit();
        }

        public void updatesAreSeenInTheUpdatedTable() {
            specify(readInNewTransaction(TABLE1, key), should.equal(value1));
        }

        public void updatesAreNotSeenInOtherTables() {
            specify(readInNewTransaction(TABLE2, key), should.equal(Blob.EMPTY_BLOB));
        }
    }

    public class WhenTheSameKeyIsUpdatedInDifferentTables {

        public void create() {
            table1.update(key, value1);
            table2.update(key, value2);
        }

        public void itDoesNotConflict() {
            tx.prepareAndCommit();
            specify(readInNewTransaction(TABLE1, key), should.equal(value1));
            specify(readInNewTransaction(TABLE2, key), should.equal(value2));
        }
    }

    public class WhenOnlyOneTableIsUpdated {

        private long firstRevision;
        private long revision;

        public void create() {
            firstRevision = dbms.getCurrentRevision();
            table1.update(key, value1);
            table2.update(key, value2);
            tx.prepareAndCommit();
            revision = dbms.getCurrentRevision();
        }

        public void revisionIsIncrementedExactlyOncePerCommit() {
            specify(revision, should.equal(firstRevision + 1));
        }

        public void tableRevisionsAreInSyncA() {
            updateInNewTransaction(TABLE1, key, value3);
            specify(dbms.getCurrentRevision(), should.equal(revision + 1));
            updateInNewTransaction(TABLE1, key, value3);
            specify(dbms.getCurrentRevision(), should.equal(revision + 2));
            specify(readInNewTransaction(TABLE1, key), should.equal(value3));
            specify(dbms.getCurrentRevision(), should.equal(revision + 3));
            specify(readInNewTransaction(TABLE2, key), should.equal(value2));
            specify(dbms.getCurrentRevision(), should.equal(revision + 4));
        }

        public void tableRevisionsAreInSyncB() {
            updateInNewTransaction(TABLE2, key, value3);
            specify(dbms.getCurrentRevision(), should.equal(revision + 1));
            updateInNewTransaction(TABLE2, key, value3);
            specify(dbms.getCurrentRevision(), should.equal(revision + 2));
            specify(readInNewTransaction(TABLE1, key), should.equal(value1));
            specify(dbms.getCurrentRevision(), should.equal(revision + 3));
            specify(readInNewTransaction(TABLE2, key), should.equal(value3));
            specify(dbms.getCurrentRevision(), should.equal(revision + 4));
        }
    }
}
