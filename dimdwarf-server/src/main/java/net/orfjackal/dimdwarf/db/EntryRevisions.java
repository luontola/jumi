/*
 * Dimdwarf Application Server
 * Copyright (c) 2008, Esko Luontola
 * All Rights Reserved.
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

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Esko Luontola
 * @since 18.8.2008
 */
public class EntryRevisions {

    private final SortedMap<Long, Blob> revisions = new TreeMap<Long, Blob>();

    public synchronized Blob read(long revision) {
        Blob value = null;
        for (Map.Entry<Long, Blob> e : revisions.entrySet()) {
            if (e.getKey() <= revision) {
                value = e.getValue();
            }
        }
        return value;
    }

    public synchronized void write(Blob value, long revision) {
        revisions.put(revision, value);
    }

    public synchronized void checkNotModifiedAfter(long revision) {
        if (revisions.size() > 0) {
            long lastWrite = latestRevision();
            if (lastWrite > revision) {
                throw new OptimisticLockException("Already modified in revision " + lastWrite);
            }
        }
    }

    public synchronized long oldestRevision() {
        return revisions.firstKey();
    }

    private synchronized long latestRevision() {
        return revisions.lastKey();
    }

    public synchronized void purgeRevisionsOlderThan(long revision) {
        if (revisions.size() >= 2) {
            SortedMap<Long, Blob> purge = revisions.headMap(Math.min(revision, latestRevision()));
            purge.clear();
        }
    }
}
