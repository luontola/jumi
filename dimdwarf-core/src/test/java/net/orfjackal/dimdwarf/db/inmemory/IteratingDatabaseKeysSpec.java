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

@RunWith(JDaveRunner.class)
@Group({"fast"})
public class IteratingDatabaseKeysSpec extends Specification<Object> {

    private static final String TABLE = "test";

    private TransactionCoordinator tx;
    private DatabaseManager dbms;
    private DatabaseTable<Blob, Blob> table;
    private Logger txLogger;

    private Blob key1 = Blob.fromBytes(new byte[]{1});
    private Blob key2 = Blob.fromBytes(new byte[]{2});
    private Blob key3 = Blob.fromBytes(new byte[]{3});
    private Blob key4 = Blob.fromBytes(new byte[]{4});
    private Blob value = Blob.fromBytes(new byte[]{42});

    public void create() throws Exception {
        dbms = new InMemoryDatabaseManager();
        txLogger = mock(Logger.class);
        beginNewTransaction();
    }

    private void beginNewTransaction() {
        tx = new TransactionContext(txLogger);
        table = dbms.openConnection(tx.getTransaction()).openTable(TABLE);
    }


    public class AnEmptyDatabaseTable {

        public void firstKey() {
            specify(table.firstKey(), should.equal(null));
        }

        public void nextKeyAfterNonexistantKey() {
            specify(table.nextKeyAfter(key1), should.equal(null));
        }
    }

    public class ANonEmptyDatabaseTable {

        public void create() {
            table.update(key1, value);
            table.update(key3, value);
            tx.prepareAndCommit();
            beginNewTransaction();
        }

        public void firstKey() {
            specify(table.firstKey(), should.equal(key1));
        }

        public void nextKeyAfterExistingKey() {
            specify(table.nextKeyAfter(key1), should.equal(key3));
        }

        public void nextKeyAfterNonexitentKey() {
            specify(table.nextKeyAfter(key2), should.equal(key3));
        }

        public void nextKeyAfterExistingLastKey() {
            specify(table.nextKeyAfter(key3), should.equal(null));
        }

        public void nextKeyAfterNonexistentLastKey() {
            specify(table.nextKeyAfter(key4), should.equal(null));
        }
    }
}
