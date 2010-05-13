// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db;

import javax.annotation.Nullable;

public interface IterableKeys<K> {

    /**
     * Returns the first key in the map, or null if it is empty.
     */
    @Nullable
    K firstKey();

    /**
     * Returns the next key after {@code currentKey} in the map, or null if the end of the map is reached.
     */
    @Nullable
    K nextKeyAfter(K currentKey);
}
