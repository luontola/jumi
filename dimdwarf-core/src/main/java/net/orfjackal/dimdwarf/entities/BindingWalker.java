// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import javax.annotation.concurrent.*;
import java.util.Iterator;

/**
 * @author Esko Luontola
 * @since 24.11.2008
 */
@Immutable
public class BindingWalker implements Iterable<String> {

    private final String prefix;
    private final BindingRepository bindings;

    public BindingWalker(String prefix, BindingRepository bindings) {
        this.prefix = prefix;
        this.bindings = bindings;
    }

    public Iterator<String> iterator() {
        return new BindingIterator(bindings.nextKeyAfter(prefix));
    }


    @NotThreadSafe
    private class BindingIterator implements Iterator<String> {

        private String next;

        public BindingIterator(String next) {
            this.next = next;
        }

        public boolean hasNext() {
            return next != null && next.startsWith(prefix);
        }

        public String next() {
            try {
                return next;
            } finally {
                next = bindings.nextKeyAfter(next);
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
