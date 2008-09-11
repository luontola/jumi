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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This class is thread-safe.
 *
 * @author Esko Luontola
 * @since 18.8.2008
 */
public class InMemoryDatabase {

    private final Map<String, InMemoryDatabaseTable> tables;
    private final Object commitLock = new Object();
    private final ConcurrentMap<Transaction, Long> openConnections = new ConcurrentHashMap<Transaction, Long>();
    private final RevisionCounter revisionCounter = new RevisionCounter();
    private volatile long committedRevision;

    public InMemoryDatabase(String... tableNames) {
        Map<String, InMemoryDatabaseTable> tables = new HashMap<String, InMemoryDatabaseTable>();
        for (String name : tableNames) {
            tables.put(name, new InMemoryDatabaseTable(revisionCounter));
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
        for (InMemoryDatabaseTable table : tables.values()) {
            table.purgeRevisionsOlderThan(getOldestUncommittedRevision());
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
        for (InMemoryDatabaseTable table : tables.values()) {
            oldest = Math.min(oldest, table.getOldestRevision());
        }
        return oldest;
    }

    private void prepareUpdates(Collection<TransactionalDatabaseTable> updates) {
        synchronized (commitLock) {
            for (TransactionalDatabaseTable update : updates) {
                update.prepare();
            }
        }
    }

    private void commitUpdates(Collection<TransactionalDatabaseTable> updates) {
        synchronized (commitLock) {
            try {
                revisionCounter.incrementRevision();
                for (TransactionalDatabaseTable update : updates) {
                    update.commit();
                }
            } finally {
                committedRevision = revisionCounter.getCurrentRevision();
                for (TransactionalDatabaseTable update : updates) {
                    update.unlockAfterCommit();
                }
            }
        }
    }

    private void rollbackUpdates(Collection<TransactionalDatabaseTable> updates) {
        synchronized (commitLock) {
            for (TransactionalDatabaseTable update : updates) {
                update.unlockAfterCommit();
            }
        }
    }


    private static class InMemoryDatabaseTable {

        private final RevisionMap<Blob, Blob> revisions;
        private final ConcurrentMap<Blob, Transaction> lockedForCommit = new ConcurrentHashMap<Blob, Transaction>();

        public InMemoryDatabaseTable(RevisionCounter revisionCounter) {
            revisions = new RevisionMap<Blob, Blob>(revisionCounter);
        }

        public Blob get(Blob key, long revision) {
            return revisions.get(key, revision);
        }

        public void purgeRevisionsOlderThan(long revisionToKeep) {
            revisions.purgeRevisionsOlderThan(revisionToKeep);
        }

        public long getOldestRevision() {
            return revisions.getOldestRevision();
        }

        private void checkForConflicts(Set<Blob> keys, long visibleRevision) {
            for (Blob key : keys) {
                long lastWrite = revisions.getLatestRevisionForKey(key);
                if (lastWrite > visibleRevision) {
                    throw new OptimisticLockException("Key " + key + " already modified in revision " + lastWrite);
                }
            }
        }

        public void lock(Transaction tx, Set<Blob> keys) {
            for (Blob key : keys) {
                Transaction alreadyLocked = lockedForCommit.putIfAbsent(key, tx);
                if (alreadyLocked != null) {
                    throw new OptimisticLockException("Key " + key + " already locked by transaction " + alreadyLocked);
                }
            }
        }

        public void commit(Transaction tx, Map<Blob, Blob> updates) {
            for (Map.Entry<Blob, Blob> update : updates.entrySet()) {
                assert lockedForCommit.get(update.getKey()).equals(tx);
                revisions.put(update.getKey(), update.getValue());
            }
        }

        private void unlock(Transaction tx, Set<Blob> keys) {
            for (Blob key : keys) {
                ConcurrentMap<Blob, Transaction> locks = lockedForCommit;
                if (locks.containsKey(key)) {
                    boolean wasLockedByMe = locks.remove(key, tx);
                    assert wasLockedByMe : "key = " + key;
                }
            }
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
            InMemoryDatabaseTable table = tables.get(name);
            if (table == null) {
                throw new IllegalArgumentException("No such table: " + name);
            }
            openTables.putIfAbsent(name, new TransactionalDatabaseTable(table, visibleRevision, tx));
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
    private static class TransactionalDatabaseTable implements DatabaseTable {

        private final Map<Blob, Blob> updates = new ConcurrentHashMap<Blob, Blob>();
        private final InMemoryDatabaseTable table;
        private final long visibleRevision;
        private final Transaction tx;

        public TransactionalDatabaseTable(InMemoryDatabaseTable table, long visibleRevision, Transaction tx) {
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

        protected void prepare() {
            table.checkForConflicts(updates.keySet(), visibleRevision);
            table.lock(tx, updates.keySet());
        }

        protected void commit() {
            table.commit(tx, updates);
        }

        protected void unlockAfterCommit() {
            table.unlock(tx, updates.keySet());
        }
    }
}
