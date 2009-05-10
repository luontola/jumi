// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.*;

/**
 * Provides a value for a key, but caches the value returned by the underlying service. The {@link #newInstance}
 * method may be called more than once, but the {@link #get} method is guaranteed to return exactly one instance
 * for a key. The cache is never emptied during the life-time of the cache instance.
 *
 * @author Esko Luontola
 * @since 7.5.2008
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
