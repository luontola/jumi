// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db.inmemory;

import com.google.inject.Singleton;
import net.orfjackal.dimdwarf.db.*;
import net.orfjackal.dimdwarf.tx.*;
import org.jetbrains.annotations.TestOnly;

import javax.annotation.concurrent.*;
import java.util.concurrent.*;

/**
 * @author Esko Luontola
 * @since 21.11.2008
 */
@Singleton
@ThreadSafe
public class InMemoryDatabaseManager implements DatabaseManager {

    private final InMemoryDatabase db = new InMemoryDatabase();
    private final ConcurrentMap<Transaction, Database<Blob, Blob>> openConnections = new ConcurrentHashMap<Transaction, Database<Blob, Blob>>();

    public Database<Blob, Blob> openConnection(Transaction tx) {
        Database<Blob, Blob> connection = getExistingConnection(tx);
        if (connection == null) {
            connection = createNewConnection(tx);
        }
        return connection;
    }

    private Database<Blob, Blob> getExistingConnection(Transaction tx) {
        return openConnections.get(tx);
    }

    private Database<Blob, Blob> createNewConnection(Transaction tx) {
        Database<Blob, Blob> con = db.createNewConnection(tx);
        Object prev = openConnections.putIfAbsent(tx, con);
        assert prev == null : "Connection " + prev + " already exists in transaction " + tx;
        tx.join(new ConnectionCloser(tx));
        return con;
    }

    private void closeConnection(Transaction tx) {
        Object removed = openConnections.remove(tx);
        assert removed != null : "No connection open in transaction " + tx;
    }

    @TestOnly
    int getOpenConnections() {
        return openConnections.size();
    }

    @TestOnly
    long getOldestRevisionInUse() {
        return db.getOldestRevisionInUse();
    }

    @TestOnly
    long getCurrentRevision() {
        return db.getCurrentRevision();
    }

    @TestOnly
    int getNumberOfKeys() {
        return db.getNumberOfKeys();
    }


    @Immutable
    private class ConnectionCloser implements TransactionParticipant {

        private final Transaction tx;

        public ConnectionCloser(Transaction tx) {
            this.tx = tx;
        }

        public void prepare() throws Throwable {
        }

        public void commit() {
            closeConnection(tx);
        }

        public void rollback() {
            closeConnection(tx);
        }
    }
}
