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

import javax.annotation.concurrent.Immutable;

/**
 * @author Esko Luontola
 * @since 12.9.2008
 */
@Immutable
public class DatabaseTableAdapter<K1, V1, K2, V2> implements DatabaseTable<K1, V1> {

    private final DatabaseTable<K2, V2> parent;
    private final Converter<K1, K2> keys;
    private final Converter<V1, V2> values;

    public DatabaseTableAdapter(DatabaseTable<K2, V2> parent,
                                Converter<K1, K2> keys,
                                Converter<V1, V2> values) {
        this.parent = parent;
        this.keys = keys;
        this.values = values;
    }

    public boolean exists(K1 key) {
        return parent.exists(keys.forth(key));
    }

    public V1 read(K1 key) {
        return values.back(parent.read(keys.forth(key)));
    }

    public void update(K1 key, V1 value) {
        parent.update(keys.forth(key), values.forth(value));
    }

    public void delete(K1 key) {
        parent.delete(keys.forth(key));
    }

    public K1 firstKey() {
        return keys.back(parent.firstKey());
    }

    public K1 nextKeyAfter(K1 currentKey) {
        return keys.back(parent.nextKeyAfter(keys.forth(currentKey)));
    }
}
