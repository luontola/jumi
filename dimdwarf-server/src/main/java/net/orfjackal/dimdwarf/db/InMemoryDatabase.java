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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This class is thread-safe.
 *
 * @author Esko Luontola
 * @since 18.8.2008
 */
public class InMemoryDatabase {

    private final Map<String, Table> tables;
    private final Object commitLock = new Object();
    private final ConcurrentMap<Transaction, Long> openConnections = new ConcurrentHashMap<Transaction, Long>();
    private final RevisionCounter revisionCounter = new RevisionCounter();
    private volatile long committedRevision;

    public InMemoryDatabase(String... tableNames) {
        Map<String, Table> tables = new HashMap<String, Table>();
        for (String name : tableNames) {
            tables.put(name, new Table(revisionCounter));
        }
        this.tables = Collections.unmodifiableMap(tables);
        committedRevision = revisionCounter.getCurrentRevision();
    }

    public Database openConnection(Transaction tx) {
        if (openConnections.containsKey(tx)) {
            throw new IllegalArgumentException("Connection already open in this transaction");
        }
        Database db = new TransactionalDatabase(committedRevision, tx);
        openConnections.put(tx, committedRevision);
        return db;
    }

    private void closeConnection(Transaction tx) {
        openConnections.remove(tx);
        for (Table table : tables.values()) {
            table.revisions.purgeRevisionsOlderThan(getOldestUncommittedRevision());
        }
    }

    protected long getOldestUncommittedRevision() {
        long oldest = committedRevision;
        for (long revision : openConnections.values()) {
            oldest = Math.min(oldest, revision);
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
        for (Table table : tables.values()) {
            oldest = Math.min(oldest, table.revisions.getOldestRevision());
        }
        return oldest;
    }

    private void prepareTransaction(Transaction tx, String tableName, Map<Blob, Blob> modified, long revision) throws Exception {
        synchronized (commitLock) {
            Table table = tables.get(tableName);
            for (Map.Entry<Blob, Blob> e : modified.entrySet()) {
                long lastWrite = table.revisions.getLatestRevisionForKey(e.getKey());
                if (lastWrite > revision) {
                    throw new OptimisticLockException("Key " + e.getKey() + " already modified in revision " + lastWrite);
                }
            }
            lockKeysForCommit(tx, tableName, modified.keySet());
        }
    }

    private void commitTransaction(Transaction tx, Map<String, TransactionalDatabaseTable> updates) {
        synchronized (commitLock) {
            try {
                revisionCounter.incrementRevision();
                for (Map.Entry<String, TransactionalDatabaseTable> e : updates.entrySet()) {
                    commitTable(tx, e.getKey(), e.getValue().updates);
                }
            } finally {
                committedRevision = revisionCounter.getCurrentRevision();
                for (Map.Entry<String, TransactionalDatabaseTable> e : updates.entrySet()) {
                    unlockKeysForCommit(tx, e.getKey(), e.getValue().updates.keySet());
                }
            }
        }
    }

    private void commitTable(Transaction tx, String tableName, Map<Blob, Blob> modified) {
        Table table = tables.get(tableName);
        for (Map.Entry<Blob, Blob> e : modified.entrySet()) {
            table.revisions.put(e.getKey(), e.getValue());
        }
    }

    private void rollbackTransaction(Transaction tx, String tableName, Map<Blob, Blob> modified) {
        synchronized (commitLock) {
            unlockKeysForCommit(tx, tableName, modified.keySet());
        }
    }

    private void lockKeysForCommit(Transaction tx, String tableName, Set<Blob> keys) {
        Table table = tables.get(tableName);
        for (Blob key : keys) {
            Transaction alreadyLocked = table.lockedForCommit.putIfAbsent(key, tx);
            if (alreadyLocked != null) {
                throw new OptimisticLockException("Key " + key + " already locked by transaction " + alreadyLocked);
            }
        }
    }

    private void unlockKeysForCommit(Transaction tx, String tableName, Set<Blob> keys) {
        Table table = tables.get(tableName);
        for (Blob key : keys) {
            ConcurrentMap<Blob, Transaction> locks = table.lockedForCommit;
            if (locks.containsKey(key)) {
                boolean wasLockedByMe = locks.remove(key, tx);
                assert wasLockedByMe : "key = " + key;
            }
        }
    }

    private static class Table {

        private final RevisionMap<Blob, Blob> revisions;
        private final ConcurrentMap<Blob, Transaction> lockedForCommit = new ConcurrentHashMap<Blob, Transaction>();

        public Table(RevisionCounter revisionCounter) {
            revisions = new RevisionMap<Blob, Blob>(revisionCounter);
        }
    }

    /**
     * This class is thread-safe.
     */
    private class TransactionalDatabase implements Database, TransactionParticipant {

        private final ConcurrentMap<String, TransactionalDatabaseTable> openTables = new ConcurrentHashMap<String, TransactionalDatabaseTable>();
        private final long visibleRevision;
        private final Transaction tx;

        public TransactionalDatabase(long visibleRevision, Transaction tx) {
            this.visibleRevision = visibleRevision;
            this.tx = tx;
            tx.join(this);
        }

        public DatabaseTable openTable(String name) {
            tx.mustBeActive();
            TransactionalDatabaseTable openTable = openTables.get(name);
            if (openTable != null) {
                return openTable;
            }
            Table table = tables.get(name);
            if (table == null) {
                throw new IllegalArgumentException("No such table: " + name);
            }
            openTables.putIfAbsent(name, new TransactionalDatabaseTable(table.revisions, visibleRevision, tx));
            return openTables.get(name);
        }

        public void joinedTransaction(Transaction tx) {
            assert this.tx == tx;
        }

        public void prepare(Transaction tx) throws Throwable {
            for (Map.Entry<String, TransactionalDatabaseTable> e : openTables.entrySet()) {
                String name = e.getKey();
                TransactionalDatabaseTable table = e.getValue();
                prepareTransaction(tx, name, table.updates, visibleRevision);
            }
        }

        public void commit(Transaction tx) {
            try {
                commitTransaction(tx, openTables);
            } finally {
                closeConnection(tx);
            }
        }

        public void rollback(Transaction tx) {
            try {
                for (Map.Entry<String, TransactionalDatabaseTable> e : openTables.entrySet()) {
                    String name = e.getKey();
                    TransactionalDatabaseTable table = e.getValue();
                    rollbackTransaction(tx, name, table.updates);
                }
            } finally {
                closeConnection(tx);
            }
        }
    }

    /**
     * This class is thread-safe.
     */
    private class TransactionalDatabaseTable implements DatabaseTable {

        private final Map<Blob, Blob> updates = new ConcurrentHashMap<Blob, Blob>();
        private final RevisionMap<Blob, Blob> revisions;
        private final long visibleRevision;
        private final Transaction tx;

        public TransactionalDatabaseTable(RevisionMap<Blob, Blob> revisions, long visibleRevision, Transaction tx) {
            this.revisions = revisions;
            this.visibleRevision = visibleRevision;
            this.tx = tx;
        }

        public Blob read(Blob key) {
            tx.mustBeActive();
            Blob blob = updates.get(key);
            if (blob == null) {
                blob = revisions.get(key, visibleRevision);
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
    }
}
