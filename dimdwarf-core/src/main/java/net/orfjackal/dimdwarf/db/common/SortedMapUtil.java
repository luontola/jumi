// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db.common;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @author Esko Luontola
 * @since 3.12.2008
 */
public class SortedMapUtil {

    private SortedMapUtil() {
    }

    @Nullable
    public static <K> K firstKey(SortedMap<K, ?> map) {
        return map.isEmpty() ? null : map.firstKey();
    }

    @Nullable
    public static <K> K nextKeyAfter(K currentKey, SortedMap<K, ?> map) {
        Iterator<K> it = map.tailMap(currentKey).keySet().iterator();
        K next;
        do {
            next = it.hasNext() ? it.next() : null;
        } while (next != null && next.equals(currentKey));
        return next;
    }
}
