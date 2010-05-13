// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db;

import javax.annotation.Nullable;

public interface DatabaseTable<K, V> extends IterableKeys<K> {

    boolean exists(K key);

    @Nullable
    V read(K key);

    void update(K key, V value);

    void delete(K key);
}
