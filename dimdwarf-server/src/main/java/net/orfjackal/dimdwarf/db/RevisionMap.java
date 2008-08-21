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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Esko Luontola
 * @since 20.8.2008
 */
public class RevisionMap<K, V> {

    private final ConcurrentMap<K, RevisionList<V>> map = new ConcurrentHashMap<K, RevisionList<V>>();
    private final Set<K> hasOldRevisions = new HashSet<K>();
    private final Object writeLock = new Object();
    private volatile long currentRevision = 0;
    private volatile long oldestRevision = 0;

    public V get(K key, long revision) {
        RevisionList<V> revs = map.get(key);
        return revs != null ? revs.get(revision) : null;
    }

    public void put(K key, V value) {
        synchronized (writeLock) {
            RevisionList<V> previous = map.get(key);
            if (previous != null && previous.latestRevision() == currentRevision) {
                throw new IllegalArgumentException("Key already set in this revision: " + key);
            }
            RevisionList<V> updated = new RevisionList<V>(currentRevision, value, previous);
            map.put(key, updated);
            if (previous != null) {
                hasOldRevisions.add(key);
            }
        }
    }

    public void remove(K key) {
        put(key, null);
    }

    public void purgeRevisionsOlderThan(long revisionToKeep) {
        synchronized (writeLock) {
            revisionToKeep = Math.min(revisionToKeep, currentRevision);
            oldestRevision = Math.max(revisionToKeep, oldestRevision);

            for (Iterator<K> purgeQueueIter = hasOldRevisions.iterator(); purgeQueueIter.hasNext();) {
                K key = purgeQueueIter.next();
                RevisionList<V> value = map.get(key);

                value.purgeRevisionsOlderThan(oldestRevision);
                if (!value.hasOldRevisions()) {
                    purgeQueueIter.remove();
                }
                if (value.isEmpty()) {
                    map.remove(key);
                }
            }
        }
    }

    public int size() {
        return map.size();
    }

    public long getCurrentRevision() {
        return currentRevision;
    }

    public long getOldestRevision() {
        return oldestRevision;
    }

    public void incrementRevision() {
        synchronized (writeLock) {
            currentRevision++;
        }
    }
}
