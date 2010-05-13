// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db;

import java.util.Set;

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
