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

package net.orfjackal.dimdwarf.db;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Map which keeps track of its modification history. Within one revision, each key may be modified only once.
 * The {@link #incrementRevision()} method must be called between every group of modifications. Once the revision
 * is incremented, the values in older revisions can not be changed. The old revisions can be only purged with
 * {@link #purgeRevisionsOlderThan(long)} when the user of this class is sure that those revisions will not be
 * accessed. The revision system used by this class was inspired by Subversion.
 * <p/>
 * This class is thread-safe.
 *
 * @author Esko Luontola
 * @since 20.8.2008
 */
public class RevisionMap<K, V> {

    private final SortedMap<K, RevisionList<V>> map = new ConcurrentSkipListMap<K, RevisionList<V>>();
    private final Set<K> hasOldRevisions = new HashSet<K>();
    private final Object writeLock = new Object();
    private volatile long currentRevision = 0;
    private volatile long oldestRevision = 0;

    public V get(K key, long revision) {
        RevisionList<V> revs = map.get(key);
        return revs != null ? revs.get(revision) : null;
    }

    public long getLatestRevisionForKey(K key) {
        RevisionList<V> revs = map.get(key);
        return revs != null ? revs.latestRevision() : currentRevision;
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

    // TODO: add support for purging sparse revisions, i.e. purgeRevisionsOtherThan(long... revisionsToKeep)
    // Will be useful for long running transactions, so that memory usage will not over 2x (with one long transaction).

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
        if (currentRevision == Long.MAX_VALUE) {
            // TODO: any good ideas on how to allow the revisions to loop freely?
            throw new Error("Numeric overflow: tried to increment past Long.MAX_VALUE");
        }
        synchronized (writeLock) {
            currentRevision++;
        }
    }

    /**
     * The iterator returned by this method is NOT thread-safe, but it allows
     * concurrent modifications of the map in other threads (as long as the revision
     * being iterated is not purged - in which case the behaviour is unspecified).
     */
    public Iterator<Map.Entry<K, V>> iterator(long revision) {
        return new MyIterator<K, V>(map, revision);
    }

    /**
     * This class is NOT thread-safe.
     */
    private static class MyIterator<K, V> implements Iterator<Map.Entry<K, V>> {
        private final long revision;
        private final Iterator<Map.Entry<K, RevisionList<V>>> it;
        private Map.Entry<K, V> fetchedNext;

        public MyIterator(SortedMap<K, RevisionList<V>> map, long revision) {
            this.revision = revision;
            it = map.entrySet().iterator();
        }

        public boolean hasNext() {
            fetchNext();
            return fetchedNext != null;
        }

        public Map.Entry<K, V> next() {
            fetchNext();
            return returnNext();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        private void fetchNext() {
            while (fetchedNext == null && it.hasNext()) {
                Map.Entry<K, RevisionList<V>> e = it.next();
                V value = e.getValue().get(revision);
                if (value != null) {
                    fetchedNext = new MyEntry<K, V>(e.getKey(), value);
                }
            }
        }

        private Map.Entry<K, V> returnNext() {
            Map.Entry<K, V> next = fetchedNext;
            if (next == null) {
                throw new NoSuchElementException();
            }
            fetchedNext = null;
            return next;
        }
    }

    /**
     * This class is immutable.
     */
    private static class MyEntry<K, V> implements Map.Entry<K, V> {
        private final K key;
        private final V value;

        public MyEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }
    }
}
