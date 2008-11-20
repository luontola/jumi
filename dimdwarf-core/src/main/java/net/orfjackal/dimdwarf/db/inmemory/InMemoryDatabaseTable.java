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

import javax.annotation.concurrent.ThreadSafe;
import java.util.*;

/**
 * @author Esko Luontola
 * @since 11.9.2008
 */
@ThreadSafe
public class InMemoryDatabaseTable implements PersistedDatabaseTable {

    private final RevisionMap<Blob, Blob> revisions = new RevisionMap<Blob, Blob>();
    private final GroupLock<Blob> keysLockedForCommit = new GroupLock<Blob>();

    public Blob firstKey() {
        return revisions.firstKey();
    }

    public Blob nextKeyAfter(Blob currentKey) {
        return revisions.nextKeyAfter(currentKey);
    }

    public Blob get(Blob key, long readRevision) {
        return revisions.get(key, readRevision);
    }

    public RevisionCommitHandle prepare(Map<Blob, Blob> updates, long readRevision) {
        return new MyCommitHandle(updates, readRevision);
    }

    public void purgeRevisionsOlderThan(long revisionToKeep) {
        revisions.purgeRevisionsOlderThan(revisionToKeep);
    }


    @ThreadSafe
    private class MyCommitHandle implements RevisionCommitHandle {

        private final Map<Blob, Blob> updates;
        private final long readRevision;
        private final LockHandle lockHandle;

        public MyCommitHandle(Map<Blob, Blob> updates, long readRevision) {
            this.updates = Collections.unmodifiableMap(new HashMap<Blob, Blob>(updates));
            this.readRevision = readRevision;
            this.lockHandle = prepare();
        }

        private LockHandle prepare() {
            LockHandle h = keysLockedForCommit.tryLock(updates.keySet());
            try {
                checkForConflicts();
            } catch (OptimisticLockException e) {
                h.unlock();
                throw e;
            }
            return h;
        }

        private void checkForConflicts() throws OptimisticLockException {
            for (Blob key : updates.keySet()) {
                checkForConcurrentModification(key);
            }
        }

        private void checkForConcurrentModification(Blob key) throws OptimisticLockException {
            long lastWrite = revisions.getLatestRevisionForKey(key);
            if (lastWrite > readRevision) {
                throw new OptimisticLockException("Key " + key + " already modified in revision " + lastWrite);
            }
        }

        public void commit(long writeRevision) {
            commitUpdates(writeRevision);
            lockHandle.unlock();
        }

        private void commitUpdates(long writeRevision) {
            for (Map.Entry<Blob, Blob> update : updates.entrySet()) {
                commitUpdate(update.getKey(), update.getValue(), writeRevision);
            }
        }

        private void commitUpdate(Blob key, Blob value, long writeRevision) {
            assert keysLockedForCommit.isLocked(key);
            revisions.put(key, value, writeRevision);
        }

        public void rollback() {
            lockHandle.unlock();
        }
    }
}
