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

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Map which keeps track of its modification history. Can be used to implement multiversion concurrency control.
 * <p/>
 * When a key is modified using revision X, the next time that same key is modified the revision must be greater
 * than X. Modifying other keys concurrently in older revisions is allowed. The old revisions can be purged with
 * {@link #purgeRevisionsOlderThan(long)} when the user of this class is sure that those revisions will not be
 * accessed.
 *
 * @author Esko Luontola
 * @since 20.8.2008
 */
@ThreadSafe
public class RevisionMap<K, V> implements IterableKeys<K> {

    private final SortedMap<K, RevisionList<V>> map = new ConcurrentSkipListMap<K, RevisionList<V>>();
    private final Set<K> hasOldRevisions = new HashSet<K>();
    private final Object writeLock = new Object();

    @Nullable
    public V get(K key, long readRevision) {
        assert readRevision >= RevisionList.NULL_REVISION;
        RevisionList<V> revs = map.get(key);
        return revs != null ? revs.get(readRevision) : null;
    }

    public void put(K key, @Nullable V value, long writeRevision) {
        synchronized (writeLock) {
            RevisionList<V> previous = map.get(key);
            checkForConcurrentModification(writeRevision, key, previous);
            map.put(key, new RevisionList<V>(writeRevision, value, previous));
            if (previous != null) {
                hasOldRevisions.add(key);
            }
        }
    }

    private static <K, V> void checkForConcurrentModification(long writeRevision, K key, @Nullable RevisionList<V> previous) {
        long lastWrite = safeLatestRevision(previous);
        if (lastWrite >= writeRevision) {
            throw new IllegalArgumentException("Key " + key + " already modified in revision " + lastWrite);
        }
    }

    public void remove(K key, long writeRevision) {
        put(key, null, writeRevision);
    }

    public long getLatestRevisionForKey(K key) {
        return safeLatestRevision(map.get(key));
    }

    private static <T> long safeLatestRevision(@Nullable RevisionList<T> revs) {
        return revs != null ? revs.getLatestRevision() : RevisionList.NULL_REVISION;
    }

    // TODO: add support for purging sparse revisions, i.e. purgeRevisionsOtherThan(long... revisionsToKeep)
    // Might be useful for long running transactions, so that this map will not expand more than 100%
    // (worst case: one long transaction reads every key, while other transactions update every key).

    public void purgeRevisionsOlderThan(long revisionToKeep) {
        synchronized (writeLock) {
            for (Iterator<K> purgeQueueIter = hasOldRevisions.iterator(); purgeQueueIter.hasNext();) {
                K key = purgeQueueIter.next();
                RevisionList<V> value = map.get(key);

                value.purgeRevisionsOlderThan(revisionToKeep);
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

    public String toString() {
        String s = getClass().getSimpleName() + "[\n";
        for (Map.Entry<K, RevisionList<V>> e : map.entrySet()) {
            s += "    " + e.getKey() + " -> " + e.getValue() + "\n";
        }
        s += "]";
        return s;
    }
}
