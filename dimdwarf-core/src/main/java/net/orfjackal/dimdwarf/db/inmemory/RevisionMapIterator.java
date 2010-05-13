// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db.inmemory;

import javax.annotation.Nullable;
import javax.annotation.concurrent.*;
import java.util.*;

@NotThreadSafe
public class RevisionMapIterator<K, V> implements Iterator<Map.Entry<K, V>> {

    private final RevisionMap<K, V> map;
    private final long readRevision;
    @Nullable private Map.Entry<K, V> fetchedNext;
    @Nullable private K nextKey;

    public RevisionMapIterator(RevisionMap<K, V> map, long readRevision) {
        this.map = map;
        this.readRevision = readRevision;
        nextKey = map.firstKey(readRevision);
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
        while (fetchedNext == null && nextKey != null) {
            K key = nextKey;
            V value = map.get(key, readRevision);
            nextKey = map.nextKeyAfter(key, readRevision);
            if (value != null) {
                fetchedNext = new MyEntry<K, V>(key, value);
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


    @Immutable
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
