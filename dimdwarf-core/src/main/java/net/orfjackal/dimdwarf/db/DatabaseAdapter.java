// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db;

import javax.annotation.concurrent.Immutable;
import java.util.Set;

@Immutable
public class DatabaseAdapter<K1, V1, K2, V2> implements Database<K1, V1> {

    private final Database<K2, V2> parent;
    private final Converter<K1, K2> keys;
    private final Converter<V1, V2> values;

    public DatabaseAdapter(Database<K2, V2> parent, Converter<K1, K2> keys, Converter<V1, V2> values) {
        this.parent = parent;
        this.keys = keys;
        this.values = values;
    }

    public IsolationLevel getIsolationLevel() {
        return parent.getIsolationLevel();
    }

    public Set<String> getTableNames() {
        return parent.getTableNames();
    }

    public DatabaseTable<K1, V1> openTable(String name) {
        return new DatabaseTableAdapter<K1, V1, K2, V2>(parent.openTable(name), keys, values);
    }
}
