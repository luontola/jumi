// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db;

import java.util.Set;

/**
 * @author Esko Luontola
 * @since 11.9.2008
 */
public interface Database<K, V> {

    IsolationLevel getIsolationLevel();

    /**
     * Existing tables.
     */
    Set<String> getTableNames();

    /**
     * Opens an existing table or creates a new table.
     */
    DatabaseTable<K, V> openTable(String name);
}
