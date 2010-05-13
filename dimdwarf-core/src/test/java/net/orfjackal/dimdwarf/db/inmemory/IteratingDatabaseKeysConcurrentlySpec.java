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
public class IteratingDatabaseKeysConcurrentlySpec extends Specification<Object> {

    private static final String TABLE = "test";

    private InMemoryDatabaseManager dbms;
    private TransactionCoordinator tx1;
    private TransactionCoordinator tx2;
    private DatabaseTable<Blob, Blob> table1;
    private DatabaseTable<Blob, Blob> table2;
    private Logger txLogger;

    private Blob key1 = Blob.fromBytes(new byte[]{1});
    private Blob key2 = Blob.fromBytes(new byte[]{2});
    private Blob key3 = Blob.fromBytes(new byte[]{3});
    private Blob key4 = Blob.fromBytes(new byte[]{4});
    private Blob value = Blob.fromBytes(new byte[]{9});

    public void create() throws Exception {
        dbms = new InMemoryDatabaseManager();
        txLogger = mock(Logger.class);
        tx1 = new TransactionContext(txLogger);
        tx2 = new TransactionContext(txLogger);
    }

    private void updateInNewTransaction(Blob key, Blob value) {
        TransactionCoordinator tx = new TransactionContext(txLogger);
        dbms.openConnection(tx.getTransaction()).openTable(TABLE).update(key, value);
        tx.prepareAndCommit();
    }


    public class WhenKeysAreCreatedInATransaction {

        public void create() {
            table1 = dbms.openConnection(tx1.getTransaction()).openTable(TABLE);
            table2 = dbms.openConnection(tx2.getTransaction()).openTable(TABLE);
            table1.update(key1, value);
            table1.update(key2, value);
        }

        public void thatTransactionMayIterateTheKeys() {
            specify(table1.firstKey(), should.equal(key1));
            specify(table1.nextKeyAfter(key1), should.equal(key2));
            specify(table1.nextKeyAfter(key2), should.equal(null));
        }

        public void theOtherTransactionCanNotIterateTheKeys() {
            specify(table2.firstKey(), should.equal(null));
            specify(table2.nextKeyAfter(key1), should.equal(null));
            specify(table2.nextKeyAfter(key2), should.equal(null));
        }

        public void evenAfterCommitTheOtherTransactionCanNotIterateTheKeys() {
            tx1.prepareAndCommit();
            theOtherTransactionCanNotIterateTheKeys();
        }
    }

    public class WhenKeysAreDeletedInATransaction {

        public void create() {
            updateInNewTransaction(key1, value);
            updateInNewTransaction(key2, value);
            table1 = dbms.openConnection(tx1.getTransaction()).openTable(TABLE);
            table2 = dbms.openConnection(tx2.getTransaction()).openTable(TABLE);
            table1.delete(key1);
            table1.delete(key2);
        }

        public void thatTransactionCanNotIterateTheKeys() {
            specify(table1.firstKey(), should.equal(null));
            specify(table1.nextKeyAfter(key1), should.equal(null));
            specify(table1.nextKeyAfter(key2), should.equal(null));
        }

        public void theOtherTransactionMayIterateTheKeys() {
            specify(table2.firstKey(), should.equal(key1));
            specify(table2.nextKeyAfter(key1), should.equal(key2));
            specify(table2.nextKeyAfter(key2), should.equal(null));
        }

        public void evenAfterCommitTheOtherTransactionMayIterateTheKeys() {
            tx1.prepareAndCommit();
            theOtherTransactionMayIterateTheKeys();
        }
    }

    public class WhenExistingAndNewlyCreatedKeysAreMixed {

        public void create() {
            updateInNewTransaction(key3, value);
            table1 = dbms.openConnection(tx1.getTransaction()).openTable(TABLE);
        }

        public void existingKeyIsFirstWhenItIsFirst() {
            table1.update(key4, value);
            specify(table1.firstKey(), should.equal(key3));
        }

        public void newlyCreatedKeyIsFirstWhenItIsFirst() {
            table1.update(key2, value);
            specify(table1.firstKey(), should.equal(key2));
        }

        public void existingKeyIsNextWhenItIsNext() {
            table1.update(key4, value);
            specify(table1.nextKeyAfter(key1), should.equal(key3));
        }

        public void newlyCreatedKeyIsNextWhenItIsNext() {
            table1.update(key2, value);
            specify(table1.nextKeyAfter(key1), should.equal(key2));
        }
    }
}
