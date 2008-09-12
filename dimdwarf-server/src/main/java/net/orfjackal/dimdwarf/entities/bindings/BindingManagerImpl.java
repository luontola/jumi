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

package net.orfjackal.dimdwarf.entities.bindings;

import net.orfjackal.dimdwarf.api.impl.EntityReference;
import net.orfjackal.dimdwarf.db.DatabaseTable;
import net.orfjackal.dimdwarf.entities.EntityLoader;
import net.orfjackal.dimdwarf.entities.EntityManager;

import java.math.BigInteger;

/**
 * This class is immutable.
 *
 * @author Esko Luontola
 * @since 12.9.2008
 */
public class BindingManagerImpl implements BindingManager {

    private final DatabaseTable<String, BigInteger> bindings;
    private final EntityManager entityManager;
    private final EntityLoader entityLoader;

    public BindingManagerImpl(DatabaseTable<String, BigInteger> bindings,
                              EntityManager entityManager,
                              EntityLoader entityLoader) {
        this.bindings = bindings;
        this.entityManager = entityManager;
        this.entityLoader = entityLoader;
    }

    public Object read(String key) {
        BigInteger id = bindings.read(key);
        return entityLoader.loadEntity(id);
    }

    public void update(String key, Object value) {
        EntityReference<Object> ref = entityManager.createReference(value);
        bindings.update(key, ref.getId());
    }

    public void delete(String key) {
        bindings.delete(key);
    }

    public String firstKey() {
        return bindings.firstKey();
    }

    public String nextKeyAfter(String currentKey) {
        return bindings.nextKeyAfter(currentKey);
    }
}
