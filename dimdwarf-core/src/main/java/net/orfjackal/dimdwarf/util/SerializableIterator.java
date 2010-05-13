// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util;

import java.io.Serializable;
import java.util.*;

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
