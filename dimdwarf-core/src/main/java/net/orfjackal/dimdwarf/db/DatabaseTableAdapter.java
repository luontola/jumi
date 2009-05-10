// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db;

import javax.annotation.concurrent.Immutable;

/**
 * @author Esko Luontola
 * @since 12.9.2008
 */
@Immutable
public class DatabaseTableAdapter<K1, V1, K2, V2> implements DatabaseTable<K1, V1> {

    private final DatabaseTable<K2, V2> parent;
    private final Converter<K1, K2> keys;
    private final Converter<V1, V2> values;

    public DatabaseTableAdapter(DatabaseTable<K2, V2> parent,
                                Converter<K1, K2> keys,
                                Converter<V1, V2> values) {
        this.parent = parent;
        this.keys = keys;
        this.values = values;
    }

    public boolean exists(K1 key) {
        return parent.exists(keys.forth(key));
    }

    public V1 read(K1 key) {
        return values.back(parent.read(keys.forth(key)));
    }

    public void update(K1 key, V1 value) {
        parent.update(keys.forth(key), values.forth(value));
    }

    public void delete(K1 key) {
        parent.delete(keys.forth(key));
    }

    public K1 firstKey() {
        return keys.back(parent.firstKey());
    }

    public K1 nextKeyAfter(K1 currentKey) {
        return keys.back(parent.nextKeyAfter(keys.forth(currentKey)));
    }
}
