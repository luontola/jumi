// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db.inmemory;

import net.orfjackal.dimdwarf.db.*;
import net.orfjackal.dimdwarf.db.common.*;
import org.jetbrains.annotations.TestOnly;

import javax.annotation.concurrent.*;
import java.util.*;

/**
 * @author Esko Luontola
 * @since 11.9.2008
 */
@ThreadSafe
public class InMemoryDatabaseTable implements PersistedDatabaseTable<RevisionHandle> {

    private final RevisionMap<Blob, Blob> revisions = new RevisionMap<Blob, Blob>();
    private final GroupLock<Blob> keysLockedForCommit = new GroupLock<Blob>();

    public Blob firstKey(RevisionHandle handle) {
        return revisions.firstKey(handle.getReadRevision());
    }

    public Blob nextKeyAfter(Blob currentKey, RevisionHandle handle) {
        return revisions.nextKeyAfter(currentKey, handle.getReadRevision());
    }

    public Blob get(Blob key, RevisionHandle handle) {
        return revisions.get(key, handle.getReadRevision());
    }

    public CommitHandle prepare(Map<Blob, Blob> updates, RevisionHandle handle) {
        return new DbTableCommitHandle(updates, handle);
    }

    public void purgeRevisionsOlderThan(long revisionToKeep) {
        revisions.purgeRevisionsOlderThan(revisionToKeep);
    }

    @TestOnly
    int getNumberOfKeys() {
        return revisions.size();
    }


    @Immutable
    private class DbTableCommitHandle implements CommitHandle {

        private final Map<Blob, Blob> updates;
        private final RevisionHandle handle;
        private final LockHandle lock;

        public DbTableCommitHandle(Map<Blob, Blob> updates, RevisionHandle handle) {
            this.updates = Collections.unmodifiableMap(new HashMap<Blob, Blob>(updates));
            this.handle = handle;
            this.lock = prepare();
        }

        private LockHandle prepare() {
            LockHandle lock = keysLockedForCommit.lockAll(updates.keySet());
            try {
                checkForConflicts();
            } catch (OptimisticLockException e) {
                lock.unlock();
                throw e;
            }
            return lock;
        }

        private void checkForConflicts() throws OptimisticLockException {
            for (Blob key : updates.keySet()) {
                checkForConcurrentModification(key);
            }
        }

        private void checkForConcurrentModification(Blob key) throws OptimisticLockException {
            long lastWrite = revisions.getLatestRevisionForKey(key);
            if (lastWrite > handle.getReadRevision()) {
                throw new OptimisticLockException("Key " + key + " already modified in revision " + lastWrite);
            }
        }

        public void commit() {
            for (Map.Entry<Blob, Blob> update : updates.entrySet()) {
                commitUpdate(update.getKey(), update.getValue());
            }
            lock.unlock();
        }

        private void commitUpdate(Blob key, Blob value) {
            assert keysLockedForCommit.isLocked(key);
            if (value.equals(Blob.EMPTY_BLOB)) {
                value = null;
            }
            revisions.put(key, value, handle.getWriteRevision());
        }

        public void rollback() {
            lock.unlock();
        }
    }
}
