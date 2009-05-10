// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db;

import javax.annotation.concurrent.ThreadSafe;
import java.util.*;

/**
 * @author Esko Luontola
 * @since 2.12.2008
 */
@ThreadSafe
public class DatabaseTableWithMetadataImpl<K, V> implements DatabaseTableWithMetadata<K, V> {

    private final Database<K, V> db;
    private final String tableName;
    private final DatabaseTable<K, V> dataTable;

    public DatabaseTableWithMetadataImpl(Database<K, V> db, String tableName) {
        this.db = db;
        this.tableName = tableName;
        this.dataTable = db.openTable(tableName);
    }

    private DatabaseTable<K, V> metaTable(String metaKey) {
        return db.openTable(tableName + META_SEPARATOR + metaKey);
    }

    private List<DatabaseTable<K, V>> allMetaTables() {
        List<DatabaseTable<K, V>> metaTables = new ArrayList<DatabaseTable<K, V>>();
        for (String s : db.getTableNames()) {
            if (s.startsWith(tableName + META_SEPARATOR)) {
                metaTables.add(db.openTable(s));
            }
        }
        return metaTables;
    }

    public V readMetadata(K key, String metaKey) {
        checkEntryExists(key);
        return metaTable(metaKey).read(key);
    }

    public void updateMetadata(K key, String metaKey, V metaValue) {
        checkEntryExists(key);
        metaTable(metaKey).update(key, metaValue);
    }

    public void deleteMetadata(K key, String metaKey) {
        checkEntryExists(key);
        metaTable(metaKey).delete(key);
    }

    public K firstEntryWithMetadata(String metaKey) {
        return metaTable(metaKey).firstKey();
    }

    private void checkEntryExists(K key) {
        if (!exists(key)) {
            throw new IllegalArgumentException("No such key: " + key);
        }
    }

    public boolean exists(K key) {
        return dataTable.exists(key);
    }

    public V read(K key) {
        return dataTable.read(key);
    }

    public void update(K key, V value) {
        dataTable.update(key, value);
    }

    public void delete(K key) {
        for (DatabaseTable<K, V> metaTable : allMetaTables()) {
            metaTable.delete(key);
        }
        dataTable.delete(key);
    }

    public K firstKey() {
        return dataTable.firstKey();
    }

    public K nextKeyAfter(K currentKey) {
        return dataTable.nextKeyAfter(currentKey);
    }
}
