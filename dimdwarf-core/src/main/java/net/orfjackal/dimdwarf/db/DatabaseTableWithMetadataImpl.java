/*
 * This file is part of Dimdwarf Application Server <http://dimdwarf.sourceforge.net/>
 *
 * Copyright (c) 2008, Esko Luontola. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *
 *     * Neither the name of the copyright holder nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
