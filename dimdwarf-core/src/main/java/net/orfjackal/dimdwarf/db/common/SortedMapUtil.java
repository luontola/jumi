// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db.common;

import javax.annotation.Nullable;
import java.util.*;

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
