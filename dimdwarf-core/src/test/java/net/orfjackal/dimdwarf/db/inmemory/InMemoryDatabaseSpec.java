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
public class InMemoryDatabaseSpec extends Specification<Object> {

    private static final String TABLE = "test";

    private InMemoryDatabaseManager dbms;
    private TransactionCoordinator tx1;
    private TransactionCoordinator tx2;

    private Blob key = Blob.fromBytes(new byte[]{0});
    private Blob value1 = Blob.fromBytes(new byte[]{1});

    public void create() throws Exception {
        dbms = new InMemoryDatabaseManager();
        Logger txLogger = mock(Logger.class);
        tx1 = new TransactionContext(txLogger);
        tx2 = new TransactionContext(txLogger);
        specify(dbms.getOpenConnections(), should.equal(0));
    }


    public class WhenMultipleDatabaseConnectionsAreOpen {

        public void create() {
            dbms.openConnection(tx1.getTransaction()).openTable(TABLE);
            dbms.openConnection(tx2.getTransaction()).openTable(TABLE);
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

        public void databaseKeepsTrackOfTheOldestRevisionInUse() {
            specify(dbms.getOldestRevisionInUse(), should.equal(0));
            tx1.prepareAndCommit();
            specify(dbms.getOldestRevisionInUse(), should.equal(0));
            tx2.prepareAndCommit();
            specify(dbms.getOldestRevisionInUse(), should.equal(2));
        }
    }

    public class WhenThereAreDatabaseEntries {

        public void create() {
            DatabaseTable<Blob, Blob> table = dbms.openConnection(tx1.getTransaction()).openTable(TABLE);
            table.update(key, value1);
            tx1.prepareAndCommit();
        }

        public void theyTakeMemorySpace() {
            specify(dbms.getNumberOfKeys(), should.equal(1));
        }

        public void afterDeleteTheMemorySpaceIsFreed() {
            DatabaseTable<Blob, Blob> table = dbms.openConnection(tx2.getTransaction()).openTable(TABLE);
            table.delete(key);
            tx2.prepareAndCommit();

            specify(dbms.getNumberOfKeys(), should.equal(0));
        }
    }
}
