/*
 * Copyright (c) 2008, Esko Luontola. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
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

import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Esko Luontola
 * @since 18.8.2008
 */
public class InMemoryDatabase {

    private final ConcurrentMap<Blob, EntryRevisions> values = new ConcurrentHashMap<Blob, EntryRevisions>();
    private final ConcurrentMap<Blob, Transaction> lockedForCommit = new ConcurrentHashMap<Blob, Transaction>();
    private final ConcurrentMap<Transaction, Integer> openConnections = new ConcurrentHashMap<Transaction, Integer>();
    private volatile int currentRevision = 1;

    public DatabaseConnection openConnection(Transaction tx) {
        if (openConnections.containsKey(tx)) {
            throw new IllegalArgumentException("Connection already open in this transaction");
        }
        TransactionalDatabaseConnection db = new TransactionalDatabaseConnection(currentRevision);
        tx.join(db);
        openConnections.put(tx, currentRevision);
        return db;
    }

    private void prepareTransaction(Transaction tx, Map<Blob, Blob> updates, int revision) throws ConcurrentModificationException {
        synchronized (lockedForCommit) {
            for (Map.Entry<Blob, Blob> entry : updates.entrySet()) {
                getCommitted(entry.getKey()).checkNotModifiedAfter(revision);
            }
            lockKeysForCommit(tx, updates.keySet());
        }
    }

    private void commitTransaction(Transaction tx, Map<Blob, Blob> updates) {
        synchronized (lockedForCommit) {
            int nextRevision = currentRevision + 1;
            try {
                for (Map.Entry<Blob, Blob> entry : updates.entrySet()) {
                    getCommitted(entry.getKey()).write(nextRevision, entry.getValue());
                }
            } finally {
                currentRevision = nextRevision;
                unlockKeysForCommit(tx, updates.keySet());
            }
        }
    }

    private void rollbackTransaction(Transaction tx, Map<Blob, Blob> updates) {
        synchronized (lockedForCommit) {
            unlockKeysForCommit(tx, updates.keySet());
        }
    }

    private void lockKeysForCommit(Transaction tx, Set<Blob> keys) {
        for (Blob key : keys) {
            Transaction alreadyLockedBy = lockedForCommit.putIfAbsent(key, tx);
            assert alreadyLockedBy == null : "key = " + key;
        }
    }

    private void unlockKeysForCommit(Transaction tx, Set<Blob> keys) {
        for (Blob key : keys) {
            if (lockedForCommit.containsKey(key)) {
                boolean wasLockedByMe = lockedForCommit.remove(key, tx);
                assert wasLockedByMe : "key = " + key;
            }
        }
    }

    private EntryRevisions getCommitted(Blob key) {
        EntryRevisions revs = values.get(key);
        if (revs == null) {
            revs = new EntryRevisions();
            values.put(key, revs);
        }
        return revs;
    }

    public int openConnections() {
        return openConnections.size();
    }


    private class TransactionalDatabaseConnection implements DatabaseConnection, TransactionParticipant {

        private final Map<Blob, Blob> updates = new ConcurrentHashMap<Blob, Blob>();
        private final int visibleRevision;
        private Transaction tx;

        public TransactionalDatabaseConnection(int visibleRevision) {
            this.visibleRevision = visibleRevision;
        }

        public void joinedTransaction(Transaction tx) {
            if (this.tx != null) {
                throw new IllegalStateException();
            }
            this.tx = tx;
        }

        public void prepare(Transaction tx) throws Throwable {
            prepareTransaction(tx, updates, visibleRevision);
        }

        public void commit(Transaction tx) {
            try {
                commitTransaction(tx, updates);
            } finally {
                openConnections.remove(tx);
            }
        }

        public void rollback(Transaction tx) {
            try {
                rollbackTransaction(tx, updates);
            } finally {
                openConnections.remove(tx);
            }
        }

        public Blob read(Blob key) {
            Blob blob = updates.get(key);
            if (blob == null) {
                blob = getCommitted(key).read(visibleRevision);
            }
            if (blob == null) {
                blob = EMPTY_BLOB;
            }
            return blob;
        }

        public void update(Blob key, Blob value) {
            updates.put(key, value);
        }

        public void delete(Blob key) {
            updates.put(key, EMPTY_BLOB);
        }
    }
}
