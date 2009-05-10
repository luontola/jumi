// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db;

/**
 * @author Esko Luontola
 * @since 2.12.2008
 */
public class DatabaseTableWithMetadataAdapter<K1, V1, K2, V2>
        extends DatabaseTableAdapter<K1, V1, K2, V2>
        implements DatabaseTableWithMetadata<K1, V1> {

    private final DatabaseTableWithMetadata<K2, V2> parent;
    private final Converter<K1, K2> keys;
    private final Converter<V1, V2> values;

    public DatabaseTableWithMetadataAdapter(DatabaseTableWithMetadata<K2, V2> parent,
                                            Converter<K1, K2> keys,
                                            Converter<V1, V2> values) {
        super(parent, keys, values);
        this.parent = parent;
        this.keys = keys;
        this.values = values;
    }

    public V1 readMetadata(K1 key, String metaKey) {
        return values.back(parent.readMetadata(keys.forth(key), metaKey));
    }

    public void updateMetadata(K1 key, String metaKey, V1 metaValue) {
        parent.updateMetadata(keys.forth(key), metaKey, values.forth(metaValue));
    }

    public void deleteMetadata(K1 key, String metaKey) {
        parent.deleteMetadata(keys.forth(key), metaKey);
    }

    public K1 firstEntryWithMetadata(String metaKey) {
        return keys.back(parent.firstEntryWithMetadata(metaKey));
    }
}
