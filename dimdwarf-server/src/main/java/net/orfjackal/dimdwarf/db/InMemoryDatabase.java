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

package net.orfjackal.dimdwarf.db;

import static net.orfjackal.dimdwarf.db.Blob.EMPTY_BLOB;
import net.orfjackal.dimdwarf.tx.Transaction;
import net.orfjackal.dimdwarf.tx.TransactionParticipant;
import org.jetbrains.annotations.TestOnly;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This class is thread-safe.
 *
 * @author Esko Luontola
 * @since 18.8.2008
 */
public class InMemoryDatabase {

    private final ConcurrentMap<Transaction, Long> revisionsInUse = new ConcurrentHashMap<Transaction, Long>();
    private final Object commitLock = new Object();

    private final Map<String, InMemoryDatabaseTable> tables;
    private final RevisionCounter revisionCounter;
    private volatile long committedRevision;

    public InMemoryDatabase(String... tableNames) {
        revisionCounter = new RevisionCounter();
        tables = Collections.unmodifiableMap(createTables(tableNames, revisionCounter));
        committedRevision = revisionCounter.getCurrentRevision();
    }

    private static Map<String, InMemoryDatabaseTable> createTables(String[] names, RevisionCounter counter) {
        Map<String, InMemoryDatabaseTable> tables = new HashMap<String, InMemoryDatabaseTable>();
        for (String name : names) {
            tables.put(name, new InMemoryDatabaseTable(counter));
        }
        return tables;
    }

    public Database openConnection(Transaction tx) {
        if (revisionsInUse.containsKey(tx)) {
            throw new IllegalArgumentException("Connection already open in this transaction");
        }
        long revision = committedRevision;
        Database db = new TxDatabase(revision, tx);
        revisionsInUse.put(tx, revision);
        return db;
    }

    private void closeConnection(Transaction tx) {
        revisionsInUse.remove(tx);
        long oldestUncommitted = getOldestUncommittedRevision();
        for (InMemoryDatabaseTable table : tables.values()) {
            table.purgeRevisionsOlderThan(oldestUncommitted);
        }
    }

    protected long getOldestUncommittedRevision() {
        long oldest = committedRevision;
        for (long revision : revisionsInUse.values()) {
            oldest = Math.min(oldest, revision);
        }
        return oldest;
    }

    @TestOnly
    protected int getOpenConnections() {
        return revisionsInUse.size();
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
    private class TxDatabase implements Database, TransactionParticipant {

        private final ConcurrentMap<String, TxDatabaseTable> openTables = new ConcurrentHashMap<String, TxDatabaseTable>();
        private final long visibleRevision;
        private final Transaction tx;

        public TxDatabase(long visibleRevision, Transaction tx) {
            this.visibleRevision = visibleRevision;
            this.tx = tx;
            tx.join(this);
        }

        public DatabaseTable openTable(String name) {
            tx.mustBeActive();
            TxDatabaseTable table = openTables.get(name);
            if (table != null) {
                return table;
            }
            return openNewTable(name);
        }

        private DatabaseTable openNewTable(String name) {
            InMemoryDatabaseTable table = tables.get(name);
            if (table == null) {
                throw new IllegalArgumentException("No such table: " + name);
            }
            openTables.putIfAbsent(name, new TxDatabaseTable(table, visibleRevision, tx));
            return openTables.get(name);
        }

        public void joinedTransaction(Transaction tx) {
            assert this.tx == tx;
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
    private static class TxDatabaseTable implements DatabaseTable {

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
