// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util;

import java.io.Serializable;
import java.util.*;

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
