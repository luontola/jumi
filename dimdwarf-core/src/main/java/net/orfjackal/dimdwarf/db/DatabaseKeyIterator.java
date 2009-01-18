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

package net.orfjackal.dimdwarf.db;

import javax.annotation.CheckForNull;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.Serializable;
import java.util.*;

/**
 * @author Esko Luontola
 * @since 3.12.2008
 */
@NotThreadSafe
public class DatabaseKeyIterator<K> implements Iterator<K>, Serializable {
    private static final long serialVersionUID = 1L;

    private transient DatabaseTable<K, ?> table;
    private K previous = null;

    public void setTable(DatabaseTable<K, ?> table) {
        this.table = table;
    }

    public boolean hasNext() {
        return nextOrNull() != null;
    }

    public K next() {
        K next = nextOrNull();
        if (next == null) {
            throw new NoSuchElementException(previous + " was the last one");
        }
        previous = next;
        return next;
    }

    @CheckForNull
    private K nextOrNull() {
        return previous == null
                ? table.firstKey()
                : table.nextKeyAfter(previous);
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
