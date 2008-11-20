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
