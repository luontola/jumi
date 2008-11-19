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

import net.orfjackal.dimdwarf.db.*;

import javax.annotation.CheckReturnValue;
import javax.annotation.concurrent.ThreadSafe;
import java.util.*;

/**
 * @author Esko Luontola
 * @since 11.9.2008
 */
@ThreadSafe
class InMemoryDatabaseTable implements PersistedDatabaseTable {

    private final RevisionMap<Blob, Blob> revisions;
    private final GroupLock<Blob> keysLockedForCommit = new GroupLock<Blob>();

    public InMemoryDatabaseTable(RevisionCounter revisionCounter) {
        revisions = new RevisionMap<Blob, Blob>(revisionCounter);
    }

    public Blob firstKey() {
        return revisions.firstKey();
    }

    public Blob nextKeyAfter(Blob currentKey) {
        return revisions.nextKeyAfter(currentKey);
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

    @CheckReturnValue
    public CommitHandle prepare(Map<Blob, Blob> updates, long revision) {
        return new MyCommitHandle(updates, revision);
    }


    private class MyCommitHandle implements CommitHandle {

        private final Map<Blob, Blob> updates;
        private final long visibleRevision;
        private LockHandle lockHandle;

        public MyCommitHandle(Map<Blob, Blob> updates, long visibleRevision) {
            this.updates = new HashMap<Blob, Blob>(updates);
            this.visibleRevision = visibleRevision;
            prepare();
        }

        private void prepare() {
            lockHandle = keysLockedForCommit.tryLock(updates.keySet());
            try {
                checkForConflicts();
            } catch (OptimisticLockException e) {
                lockHandle.unlock();
                throw e;
            }
        }

        private void checkForConflicts() throws OptimisticLockException {
            for (Blob key : updates.keySet()) {
                checkForModifiedInOtherTransaction(key);
            }
        }

        private void checkForModifiedInOtherTransaction(Blob key) throws OptimisticLockException {
            long lastWrite = revisions.getLatestRevisionForKey(key);
            if (lastWrite > visibleRevision) {
                throw new OptimisticLockException("Key " + key + " already modified in revision " + lastWrite);
            }
        }

        public void commit() {
            commitUpdates();
            lockHandle.unlock();
        }

        private void commitUpdates() {
            for (Map.Entry<Blob, Blob> update : updates.entrySet()) {
                commitUpdate(update.getKey(), update.getValue());
            }
        }

        private void commitUpdate(Blob key, Blob value) {
            assert keysLockedForCommit.isLocked(key);
            revisions.put(key, value);
        }

        public void rollback() {
            lockHandle.unlock();
        }
    }
}
