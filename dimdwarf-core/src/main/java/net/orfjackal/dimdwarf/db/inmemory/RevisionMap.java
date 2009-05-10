// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db.inmemory;

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
public class RevisionMap<K, V> {

    private final SortedMap<K, RevisionList<V>> map = new ConcurrentSkipListMap<K, RevisionList<V>>();
    private final Set<K> hasOldRevisions = new HashSet<K>();
    private final Object writeLock = new Object();

    public boolean exists(K key, long readRevision) {
        RevisionList<V> revs = map.get(key);
        return revs != null && revs.get(readRevision) != null;
    }

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

    @Nullable
    public K firstKey(long readRevision) {
        K first = SortedMapUtil.firstKey(map);
        if (first != null && !exists(first, readRevision)) {
            first = nextKeyAfter(first, readRevision);
        }
        return first;
    }

    @Nullable
    public K nextKeyAfter(K currentKey, long readRevision) {
        K next = currentKey;
        do {
            next = SortedMapUtil.nextKeyAfter(next, map);
        } while (next != null && !exists(next, readRevision));
        return next;
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

    public String toString() {
        String s = getClass().getSimpleName() + "[\n";
        for (Map.Entry<K, RevisionList<V>> e : map.entrySet()) {
            s += "    " + e.getKey() + " -> " + e.getValue() + "\n";
        }
        s += "]";
        return s;
    }
}
