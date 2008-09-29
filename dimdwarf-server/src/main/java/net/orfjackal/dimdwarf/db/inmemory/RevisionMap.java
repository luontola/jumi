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

import net.orfjackal.dimdwarf.db.IterableKeys;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Map which keeps track of its modification history. Within one revision, each key may be modified only once.
 * The {@link RevisionCounter#incrementRevision()} method of the injected counter must be called between every
 * group of modifications. Once the revision is incremented, the values in older revisions can not be changed.
 * <p/>
 * The old revisions can be purged with {@link #purgeRevisionsOlderThan(long)} when the user of this class is sure
 * that those revisions will not be accessed. The revision system used by this class was inspired by Subversion.
 * <p/>
 * Also see:
 * <a href="http://en.wikipedia.org/wiki/Multiversion_concurrency_control">multiversion concurrency control</a>,
 * <a href="http://en.wikipedia.org/wiki/Timestamp-based_concurrency_control">timestamp-based concurrency control</a>,
 * <a href="http://en.wikipedia.org/wiki/Snapshot_isolation">snapshot isolation</a>
 * <p/>
 * This class is thread-safe.
 *
 * @author Esko Luontola
 * @since 20.8.2008
 */
public class RevisionMap<K, V> implements IterableKeys<K> {

    private final SortedMap<K, RevisionList<V>> map = new ConcurrentSkipListMap<K, RevisionList<V>>();
    private final Set<K> hasOldRevisions = new HashSet<K>();
    private final Object writeLock = new Object();
    private final RevisionCounter counter;
    private volatile long oldestRevision = 0;

    public RevisionMap(RevisionCounter counter) {
        this.counter = counter;
    }

    public V get(K key, long revision) {
        RevisionList<V> revs = map.get(key);
        return revs != null ? revs.get(revision) : null;
    }

    public long getLatestRevisionForKey(K key) {
        RevisionList<V> revs = map.get(key);
        return revs != null ? revs.latestRevision() : counter.getCurrentRevision();
    }

    public void put(K key, V value) {
        synchronized (writeLock) {
            RevisionList<V> previous = map.get(key);
            if (previous != null && previous.latestRevision() == counter.getCurrentRevision()) {
                throw new IllegalArgumentException("Key already set in this revision: " + key);
            }
            RevisionList<V> updated = new RevisionList<V>(counter.getCurrentRevision(), value, previous);
            map.put(key, updated);
            if (previous != null) {
                hasOldRevisions.add(key);
            }
        }
    }

    public void remove(K key) {
        put(key, null);
    }

    // TODO: add support for purging sparse revisions, i.e. purgeRevisionsOtherThan(long... revisionsToKeep)
    // Will be useful for long running transactions, so that memory usage will not over 2x (with one long transaction).

    public void purgeRevisionsOlderThan(long revisionToKeep) {
        synchronized (writeLock) {
            revisionToKeep = Math.min(revisionToKeep, counter.getCurrentRevision());
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

    public long getOldestRevision() {
        return oldestRevision;
    }

    public K firstKey() {
        return map.isEmpty() ? null : map.firstKey();
    }

    public K nextKeyAfter(K currentKey) {
        Iterator<K> it = map.tailMap(currentKey).keySet().iterator();
        K next;
        do {
            next = it.hasNext() ? it.next() : null;
        } while (next != null && next.equals(currentKey));
        return next;
    }
}
