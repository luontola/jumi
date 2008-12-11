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

package net.orfjackal.dimdwarf.gc.entities;

import com.google.inject.Inject;
import net.orfjackal.dimdwarf.entities.*;
import net.orfjackal.dimdwarf.entities.dao.BindingDao;
import net.orfjackal.dimdwarf.gc.MutatorListener;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.math.BigInteger;

/**
 * @author Esko Luontola
 * @since 12.9.2008
 */
@Immutable
public class GcAwareBindingRepository implements BindingRepository {

    private final BindingDao bindings;
    private final ConvertEntityToEntityId idConverter;
    private final MutatorListener<BigInteger> listener;

    @Inject
    public GcAwareBindingRepository(BindingDao bindings,
                                    ConvertEntityToEntityId idConverter,
                                    MutatorListener<BigInteger> listener) {
        this.bindings = bindings;
        this.idConverter = idConverter;
        this.listener = listener;
    }

    public boolean exists(String binding) {
        return bindings.exists(binding);
    }

    public Object read(String binding) {
        return entityFromId(bindings.read(binding));
    }

    private Object entityFromId(BigInteger id) {
        return idConverter.back(id);
    }

    public void update(String binding, Object entity) {
        BigInteger prevId = bindings.read(binding);
        BigInteger nextId = idFromEntity(entity);
        bindings.update(binding, nextId);
        fireBindingChanged(prevId, nextId);
    }

    private BigInteger idFromEntity(Object entity) {
        return idConverter.forth(entity);
    }

    public void delete(String binding) {
        BigInteger prevId = bindings.read(binding);
        bindings.delete(binding);
        fireBindingChanged(prevId, null);
    }

    private void fireBindingChanged(@Nullable BigInteger prevId, @Nullable BigInteger nextId) {
        if (prevId != null) {
            listener.onReferenceRemoved(null, prevId);
        }
        if (nextId != null) {
            listener.onReferenceCreated(null, nextId);
        }
    }

    public String firstKey() {
        return bindings.firstKey();
    }

    public String nextKeyAfter(String currentKey) {
        return bindings.nextKeyAfter(currentKey);
    }
}
