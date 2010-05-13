// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.*;

/**
 * Provides a value for a key, but caches the value returned by the underlying service. The {@link #newInstance}
 * method may be called more than once, but the {@link #get} method is guaranteed to return exactly one instance
 * for a key. The cache is never emptied during the life-time of the cache instance.
 */
@ThreadSafe
public abstract class SingletonCache<K, V> {

    private final ConcurrentMap<K, V> cache = new ConcurrentHashMap<K, V>();

    public V get(K key) {
        V value = cache.get(key);
        if (value == null) {
            cache.putIfAbsent(key, newInstance(key));
            value = cache.get(key);
        }
        return value;
    }

    protected abstract V newInstance(K key);
}
