// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db;

import javax.annotation.Nullable;

public interface DatabaseTable<K, V> extends IterableKeys<K> {

    boolean exists(K key);

    @Nullable
    V read(K key);

    void update(K key, V value);

    void delete(K key);
}
