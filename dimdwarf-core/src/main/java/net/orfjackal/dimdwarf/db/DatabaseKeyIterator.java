// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db;

import javax.annotation.CheckForNull;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.Serializable;
import java.util.*;

/**
 * @author Esko Luontola
 * @since 3.12.2008
 */
@NotThreadSafe
public class DatabaseKeyIterator<K> implements Iterator<K>, Serializable {
    private static final long serialVersionUID = 1L;

    private transient DatabaseTable<K, ?> table;
    private K previous = null;

    public void setTable(DatabaseTable<K, ?> table) {
        this.table = table;
    }

    public boolean hasNext() {
        return nextOrNull() != null;
    }

    public K next() {
        K next = nextOrNull();
        if (next == null) {
            throw new NoSuchElementException(previous + " was the last one");
        }
        previous = next;
        return next;
    }

    @CheckForNull
    private K nextOrNull() {
        return previous == null
                ? table.firstKey()
                : table.nextKeyAfter(previous);
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
