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

import com.google.inject.Inject;
import net.orfjackal.dimdwarf.api.EntityInfo;
import net.orfjackal.dimdwarf.db.Converter;

import javax.annotation.concurrent.Immutable;
import java.math.BigInteger;

/**
 * @author Esko Luontola
 * @since 12.9.2008
 */
@Immutable
public class ConvertEntityToEntityId implements Converter<Object, BigInteger> {

    private final EntityManager entityManager;
    private final EntityInfo entityInfo;

    @Inject
    public ConvertEntityToEntityId(EntityManager entityManager, EntityInfo entityInfo) {
        this.entityManager = entityManager;
        this.entityInfo = entityInfo;
    }

    public Object back(BigInteger id) {
        if (id == null) {
            return null;
        }
        return entityManager.getEntityById(id);
    }

    public BigInteger forth(Object entity) {
        if (entity == null) {
            return null;
        }
        // EntityInfo must be used instead of EntityManager, because the object
        // could be a transparent reference proxy, and EntityManager does not
        // know how to handle transparent references.
        return entityInfo.getEntityId(entity);
    }
}
