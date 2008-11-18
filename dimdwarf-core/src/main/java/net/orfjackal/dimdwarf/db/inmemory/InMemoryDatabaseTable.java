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

import net.orfjackal.dimdwarf.db.Blob;
import net.orfjackal.dimdwarf.db.IterableKeys;
import net.orfjackal.dimdwarf.db.OptimisticLockException;
import net.orfjackal.dimdwarf.tx.Transaction;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This class is thread-safe.
 *
 * @author Esko Luontola
 * @since 11.9.2008
 */
class InMemoryDatabaseTable implements IterableKeys<Blob> {

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

    // TODO: This check, lock, putAll, unlock sequence does not force the right order of calling the methods.
    // It would be better to create an API like this:
    // CommitHandle handle = prepare(updatedData, revision); handle.commit/rollback();

    public synchronized void checkForConflicts(Set<Blob> keys, long visibleRevision) {
        for (Blob key : keys) {
            long lastWrite = revisions.getLatestRevisionForKey(key);
            if (lastWrite > visibleRevision) {
                throw new OptimisticLockException("Key " + key + " already modified in revision " + lastWrite);
            }
        }
    }

    public synchronized void lock(Transaction tx, Set<Blob> keys) {
        for (Blob key : keys) {
            Transaction alreadyLocked = lockedForCommit.putIfAbsent(key, tx);
            if (alreadyLocked != null) {
                throw new OptimisticLockException("Key " + key + " already locked by transaction " + alreadyLocked);
            }
        }
    }

    public synchronized void putAll(Transaction tx, Map<Blob, Blob> updates) {
        for (Map.Entry<Blob, Blob> update : updates.entrySet()) {
            assert lockedForCommit.get(update.getKey()).equals(tx);
            revisions.put(update.getKey(), update.getValue());
        }
    }

    public synchronized void unlock(Transaction tx, Set<Blob> keys) {
        for (Blob key : keys) {
            ConcurrentMap<Blob, Transaction> locks = lockedForCommit;
            if (locks.containsKey(key)) {
                boolean wasLockedByMe = locks.remove(key, tx);
                assert wasLockedByMe : "key = " + key;
            }
        }
    }

    public Blob firstKey() {
        return revisions.firstKey();
    }

    public Blob nextKeyAfter(Blob currentKey) {
        return revisions.nextKeyAfter(currentKey);
    }
}
