// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.scheduler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Esko Luontola
 * @since 26.11.2008
 */
public class FakeRecoverableSet<T> implements RecoverableSet<T> {

    private Map<String, T> values = new ConcurrentHashMap<String, T>();

    public String put(T value) {
        String key = String.valueOf(value.hashCode());
        values.put(key, value);
        return key;
    }

    public T remove(String key) {
        return values.remove(key);
    }

    public T get(String key) {
        return values.get(key);
    }

    public Collection<T> getAll() {
        return values.values();
    }
}
