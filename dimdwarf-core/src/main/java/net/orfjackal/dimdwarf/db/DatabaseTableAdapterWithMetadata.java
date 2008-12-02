/*
 * This file is part of Dimdwarf Application Server <http://dimdwarf.sourceforge.net/>
 *
 * Copyright (c) 2008, Esko Luontola. All Rights Reserved.
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
 *     * Neither the name of the copyright holder nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
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

/**
 * @author Esko Luontola
 * @since 2.12.2008
 */
public class DatabaseTableAdapterWithMetadata<K1, V1, K2, V2>
        extends DatabaseTableAdapter<K1, V1, K2, V2>
        implements DatabaseTableWithMetadata<K1, V1> {

    private final DatabaseTableWithMetadata<K2, V2> parent;
    private final Converter<K1, K2> keys;
    private final Converter<V1, V2> values;

    public DatabaseTableAdapterWithMetadata(DatabaseTableWithMetadata<K2, V2> parent, Converter<K1, K2> keys, Converter<V1, V2> values) {
        super(parent, keys, values);
        this.parent = parent;
        this.keys = keys;
        this.values = values;
    }

    public V1 readMetadata(K1 key, String metaKey) {
        return values.back(parent.readMetadata(keys.forth(key), metaKey));
    }

    public void updateMetadata(K1 key, String metaKey, V1 metaValue) {
        parent.updateMetadata(keys.forth(key), metaKey, values.forth(metaValue));
    }

    public void deleteMetadata(K1 key, String metaKey) {
        parent.deleteMetadata(keys.forth(key), metaKey);
    }
}
