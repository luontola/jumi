/*
 * This file is part of Dimdwarf Application Server <http://dimdwarf.sourceforge.net/>
 *
 * Copyright (c) 2008-2009, Esko Luontola. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
