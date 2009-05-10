// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db.inmemory;

import org.jetbrains.annotations.TestOnly;

import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Esko Luontola
 * @since 11.9.2008
 */
@ThreadSafe
public class RevisionCounter {

    private final Collection<RevisionHandle> revisionsInUse = new ConcurrentLinkedQueue<RevisionHandle>();
    private final AtomicLong writeRevision = new AtomicLong(RevisionList.NULL_REVISION);
    private volatile long oldestReadableRevision = RevisionList.NULL_REVISION;
    private volatile long newestReadableRevision = RevisionList.NULL_REVISION;
    private volatile long newestCommittedRevision = RevisionList.NULL_REVISION;

    public long getOldestReadableRevision() {
        return oldestReadableRevision;
    }

    public long getNewestReadableRevision() {
        return newestReadableRevision;
    }

    public RevisionHandle openNewestRevision() {
        RevisionHandle h = new RevisionHandle(getNewestReadableRevision(), this);
        revisionsInUse.add(h);
        return h;
    }

    long nextWriteRevision() {
        return checkForOverflow(writeRevision.incrementAndGet());
    }

    void rollback(RevisionHandle handle) {
        revisionsInUse.remove(handle);
    }

    void commitWrites(RevisionHandle handle) {
        revisionsInUse.remove(handle);
        updateReadableRevisions(handle.getWriteRevision());
    }

    private synchronized void updateReadableRevisions(long currentlyCommittedRevision) {
        newestCommittedRevision = Math.max(newestCommittedRevision, currentlyCommittedRevision);
        newestReadableRevision = getOldestCommittedRevision(newestCommittedRevision);
        oldestReadableRevision = getOldestRevisionInUse(newestCommittedRevision);
    }

    private long getOldestCommittedRevision(long newestCommitted) {
        long oldest = newestCommitted;
        for (RevisionHandle h : revisionsInUse) {
            if (h.isWriteRevisionPrepared()) {
                oldest = Math.min(oldest, h.getWriteRevision() - 1);
            }
        }
        return oldest;
    }

    private long getOldestRevisionInUse(long newestCommitted) {
        long oldest = newestCommitted;
        for (RevisionHandle h : revisionsInUse) {
            oldest = Math.min(oldest, h.getReadRevision());
        }
        return oldest;
    }

    public String toString() {
        return getClass().getSimpleName()
                + "[readableRange=" + oldestReadableRevision + "-" + newestReadableRevision
                + ", newestCommitted=" + newestCommittedRevision
                + ", revisionsInUse=" + revisionsInUse
                + "]";
    }

    private static long checkForOverflow(long x) {
        // TODO: any good ideas on how to allow the revisions to loop freely?
        if (x < RevisionList.NULL_REVISION) {
            throw new Error("Numeric overflow has happened");
        }
        return x;
    }

    @TestOnly
    Collection<Long> getRevisionsInUse() {
        ArrayList<Long> revisions = new ArrayList<Long>();
        for (RevisionHandle h : revisionsInUse) {
            revisions.add(h.getReadRevision());
        }
        return Collections.unmodifiableCollection(revisions);
    }
}
