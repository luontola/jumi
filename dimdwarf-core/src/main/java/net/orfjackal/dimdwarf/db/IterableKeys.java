// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db;

import javax.annotation.Nullable;


/**
 * @author Esko Luontola
 * @since 12.9.2008
 */
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
