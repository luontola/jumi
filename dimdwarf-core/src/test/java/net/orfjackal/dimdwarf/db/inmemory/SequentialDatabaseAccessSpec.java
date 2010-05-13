// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db.inmemory;

import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.db.*;
import net.orfjackal.dimdwarf.tx.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

import static net.orfjackal.dimdwarf.db.Blob.EMPTY_BLOB;

@RunWith(JDaveRunner.class)
@Group({"fast"})
public class SequentialDatabaseAccessSpec extends Specification<Object> {

    private static final String TABLE = "test";

    private InMemoryDatabaseManager dbms;
    private Database<Blob, Blob> db;
    private DatabaseTable<Blob, Blob> table;
    private TransactionCoordinator tx;

    private Blob key = Blob.fromBytes(new byte[]{1});
    private Blob value = Blob.fromBytes(new byte[]{2});
    private Blob otherValue = Blob.fromBytes(new byte[]{3});

    public void create() throws Exception {
        dbms = new InMemoryDatabaseManager();
        tx = new TransactionContext(mock(Logger.class));
        db = dbms.openConnection(tx.getTransaction());
        table = db.openTable(TABLE);
    }

    private void canNotBeUsed(final Database<Blob, Blob> db, final DatabaseTable<Blob, Blob> table) {
        specify(new Block() {
            public void run() throws Throwable {
                db.openTable(TABLE);
            }
        }, should.raise(TransactionRequiredException.class));
        specify(new Block() {
            public void run() throws Throwable {
                table.exists(key);
            }
        }, should.raise(TransactionRequiredException.class));
        specify(new Block() {
            public void run() throws Throwable {
                table.read(key);
            }
        }, should.raise(TransactionRequiredException.class));
        specify(new Block() {
            public void run() throws Throwable {
                table.update(key, value);
            }
        }, should.raise(TransactionRequiredException.class));
        specify(new Block() {
            public void run() throws Throwable {
                table.delete(key);
            }
        }, should.raise(TransactionRequiredException.class));
        specify(new Block() {
            public void run() throws Throwable {
                table.firstKey();
            }
        }, should.raise(TransactionRequiredException.class));
        specify(new Block() {
            public void run() throws Throwable {
                table.nextKeyAfter(key);
            }
        }, should.raise(TransactionRequiredException.class));
    }


    public class WhenDatabaseConnectionIsOpened {

        public void theConnectionIsOpen() {
            specify(db, should.not().equal(null));
            specify(table, should.not().equal(null));
            specify(dbms.getOpenConnections(), should.equal(1));
        }

        public void onlyOneConnectionExistsPerTransaction() {
            specify(dbms.openConnection(tx.getTransaction()), should.equal(db));
            specify(dbms.getOpenConnections(), should.equal(1));
        }

        public void connectionCanNotBeUsedAfterPrepare() {
            tx.prepare();
            canNotBeUsed(db, table);
        }

        public void connectionCanNotBeUsedAfterCommit() {
            tx.prepare();
            tx.commit();
            canNotBeUsed(db, table);
        }

        public void connectionCanNotBeUsedAfterRollback() {
            tx.rollback();
            canNotBeUsed(db, table);
        }

        public void connectionCanNotBeUsedAfterPrepareAndRollback() {
            tx.prepare();
            tx.rollback();
            canNotBeUsed(db, table);
        }
    }

    public class WhenEntryDoesNotExist {

        public void itDoesNotExist() {
            specify(table.exists(key), should.equal(false));
        }

        public void itHasAnEmptyValue() {
            specify(table.read(key), should.equal(EMPTY_BLOB));
        }
    }

    public class WhenEntryIsCreated {

        public void create() {
            table.update(key, value);
        }

        public void theEntryExists() {
            specify(table.exists(key));
        }

        public void itsValueCanBeRead() {
            specify(table.read(key), should.equal(value));
        }
    }

    public class WhenEntryIsUpdated {

        public void create() {
            table.update(key, value);
            table.update(key, otherValue);
        }

        public void itsLatestValueCanBeRead() {
            specify(table.read(key), should.equal(otherValue));
        }
    }

    public class WhenEntryIsDeleted {

        public void create() {
            table.update(key, value);
            table.delete(key);
        }

        public void itDoesNotExistAnymore() {
            specify(table.exists(key), should.equal(false));
        }

        public void itHasAnEmptyValue() {
            specify(table.read(key), should.equal(EMPTY_BLOB));
        }
    }
}
