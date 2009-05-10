// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util;

import java.io.Serializable;
import java.util.*;

/**
 * @author Esko Luontola
 * @since 10.12.2008
 */
public class SerializableIterable<T> implements Iterable<T>, Serializable {
    private static final long serialVersionUID = 1L;

    private final List<T> list;

    public SerializableIterable(List<T> list) {
        this.list = list;
    }

    public Iterator<T> iterator() {
        return new SerializableIterator<T>(list);
    }
}
