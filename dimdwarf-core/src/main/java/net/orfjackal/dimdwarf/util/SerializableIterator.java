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
public class SerializableIterator<T> implements Iterator<T>, Serializable {
    private static final long serialVersionUID = 1L;

    private final List<T> list;
    private int next;

    public SerializableIterator(List<T> list) {
        this.list = list;
        this.next = 0;
    }

    public boolean hasNext() {
        return next < list.size();
    }

    public T next() {
        return list.get(next++);
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
