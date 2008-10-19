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

import com.google.inject.Singleton;
import net.orfjackal.dimdwarf.db.*;
import static net.orfjackal.dimdwarf.db.Blob.EMPTY_BLOB;
import net.orfjackal.dimdwarf.tx.Transaction;
import net.orfjackal.dimdwarf.tx.TransactionParticipant;
import org.jetbrains.annotations.TestOnly;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * An in-memory database which uses multiversion concurrency control.
 * The isolation mode is snapshot isolation - it allows write skew, but prevents phantom reads.
 * <p/>
 * See:
 * <a href="http://en.wikipedia.org/wiki/Multiversion_concurrency_control">multiversion concurrency control</a>,
 * <a href="http://en.wikipedia.org/wiki/Snapshot_isolation">snapshot isolation</a>
 * <p/>
 * This class is thread-safe.
 *
 * @author Esko Luontola
 * @since 18.8.2008
 */
@Singleton
public class InMemoryDatabase implements DatabaseManager {

    private final ConcurrentMap<Transaction, TxDatabase> openConnections = new ConcurrentHashMap<Transaction, TxDatabase>();
    private final Object commitLock = new Object();

    private final ConcurrentMap<String, InMemoryDatabaseTable> tables = new ConcurrentHashMap<String, InMemoryDatabaseTable>();
    private final RevisionCounter revisionCounter;
    private volatile long committedRevision;

    public InMemoryDatabase() {
        revisionCounter = new RevisionCounter();
        committedRevision = revisionCounter.getCurrentRevision();
    }

    // Tables

    private InMemoryDatabaseTable openOrCreate(String tableName) {
        InMemoryDatabaseTable table = getExistingTable(tableName);
        if (table == null) {
            table = createNewTable(tableName);
        }
        return table;
    }

    private InMemoryDatabaseTable getExistingTable(String tableName) {
        return tables.get(tableName);
    }

    private InMemoryDatabaseTable createNewTable(String tableName) {
        tables.putIfAbsent(tableName, new InMemoryDatabaseTable(revisionCounter));
        return getExistingTable(tableName);
    }

    // Connections

    public Database<Blob, Blob> openConnection(Transaction tx) {
        TxDatabase db = getExistingConnection(tx);
        if (db == null) {
            db = createNewConnection(tx);
        }
        return db;
    }

    private InMemoryDatabase.TxDatabase getExistingConnection(Transaction tx) {
        return openConnections.get(tx);
    }

    private TxDatabase createNewConnection(Transaction tx) {
        openConnections.putIfAbsent(tx, new TxDatabase(committedRevision, tx));
        return getExistingConnection(tx);
    }

    private void closeConnection(Transaction tx) {
        openConnections.remove(tx);
        purgeOldUnusedRevisions();
    }

    private void purgeOldUnusedRevisions() {
        long oldestUncommitted = getOldestUncommittedRevision();
        for (InMemoryDatabaseTable table : tables.values()) {
            table.purgeRevisionsOlderThan(oldestUncommitted);
        }
    }

    protected long getOldestUncommittedRevision() {
        long oldest = committedRevision;
        for (TxDatabase db : openConnections.values()) {
            oldest = Math.min(oldest, db.visibleRevision);
        }
        return oldest;
    }

    @TestOnly
    protected int getOpenConnections() {
        return openConnections.size();
    }

    @TestOnly
    protected long getCurrentRevision() {
        return committedRevision;
    }

    @TestOnly
    protected long getOldestStoredRevision() {
        long oldest = revisionCounter.getCurrentRevision();
        for (InMemoryDatabaseTable table : tables.values()) {
            oldest = Math.min(oldest, table.getOldestRevision());
        }
        return oldest;
    }

    // Transactions

    private void prepareUpdates(Collection<TxDatabaseTable> updates) {
        synchronized (commitLock) {
            for (TxDatabaseTable update : updates) {
                update.prepare();
            }
        }
    }

    private void commitUpdates(Collection<TxDatabaseTable> updates) {
        synchronized (commitLock) {
            try {
                revisionCounter.incrementRevision();
                for (TxDatabaseTable update : updates) {
                    update.commit();
                }
            } finally {
                committedRevision = revisionCounter.getCurrentRevision();
                for (TxDatabaseTable update : updates) {
                    update.releaseLocks();
                }
            }
        }
    }

    private void rollbackUpdates(Collection<TxDatabaseTable> updates) {
        synchronized (commitLock) {
            for (TxDatabaseTable update : updates) {
                update.releaseLocks();
            }
        }
    }


    /**
     * This class is thread-safe.
     */
    private class TxDatabase implements Database<Blob, Blob>, TransactionParticipant {

        private final ConcurrentMap<String, TxDatabaseTable> openTables = new ConcurrentHashMap<String, TxDatabaseTable>();
        private final long visibleRevision;
        private final Transaction tx;

        public TxDatabase(long visibleRevision, Transaction tx) {
            this.visibleRevision = visibleRevision;
            this.tx = tx;
            tx.join(this);
        }

        public IsolationLevel getIsolationLevel() {
            return IsolationLevel.SNAPSHOT;
        }

        public Set<String> tables() {
            return tables.keySet();
        }

        public DatabaseTable<Blob, Blob> openTable(String name) {
            tx.mustBeActive();
            TxDatabaseTable table = openTables.get(name);
            if (table == null) {
                openTables.putIfAbsent(name, new TxDatabaseTable(openOrCreate(name), visibleRevision, tx));
                table = openTables.get(name);
            }
            return table;
        }

        public void prepare(Transaction tx) throws Throwable {
            prepareUpdates(openTables.values());
        }

        public void commit(Transaction tx) {
            try {
                commitUpdates(openTables.values());
            } finally {
                closeConnection(tx);
            }
        }

        public void rollback(Transaction tx) {
            try {
                rollbackUpdates(openTables.values());
            } finally {
                closeConnection(tx);
            }
        }
    }

    /**
     * This class is thread-safe.
     */
    private static class TxDatabaseTable implements DatabaseTable<Blob, Blob> {

        private final Map<Blob, Blob> updates = new ConcurrentHashMap<Blob, Blob>();
        private final InMemoryDatabaseTable table;
        private final long visibleRevision;
        private final Transaction tx;

        public TxDatabaseTable(InMemoryDatabaseTable table, long visibleRevision, Transaction tx) {
            this.table = table;
            this.visibleRevision = visibleRevision;
            this.tx = tx;
        }

        public Blob read(Blob key) {
            tx.mustBeActive();
            Blob blob = updates.get(key);
            if (blob == null) {
                blob = table.get(key, visibleRevision);
            }
            if (blob == null) {
                blob = EMPTY_BLOB;
            }
            return blob;
        }

        public void update(Blob key, Blob value) {
            tx.mustBeActive();
            updates.put(key, value);
        }

        public void delete(Blob key) {
            tx.mustBeActive();
            updates.put(key, EMPTY_BLOB);
        }

        // TODO: 'firstKey' and 'nextKeyAfter' do not see keys which were created during this transaction

        public Blob firstKey() {
            tx.mustBeActive();
            return table.firstKey();
        }

        public Blob nextKeyAfter(Blob currentKey) {
            tx.mustBeActive();
            return table.nextKeyAfter(currentKey);
        }

        private void prepare() {
            table.checkForConflicts(updates.keySet(), visibleRevision);
            table.lock(tx, updates.keySet());
        }

        private void commit() {
            table.putAll(tx, updates);
        }

        private void releaseLocks() {
            table.unlock(tx, updates.keySet());
        }
    }
}
