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

package net.orfjackal.dimdwarf.entities;

import com.google.inject.Inject;
import net.orfjackal.dimdwarf.api.internal.Entities;
import net.orfjackal.dimdwarf.db.Blob;
import net.orfjackal.dimdwarf.db.Converter;
import net.orfjackal.dimdwarf.serial.ObjectSerializer;

/**
 * This class is immutable.
 *
 * @author Esko Luontola
 * @since 12.9.2008
 */
public class ConvertEntityToBytes implements Converter<Object, Blob> {

    private final ObjectSerializer serializer;

    @Inject
    public ConvertEntityToBytes(ObjectSerializer serializer) {
        this.serializer = serializer;
    }

    public Object back(Blob value) {
        if (value == null) {
            return null;
        }
        if (value.equals(Blob.EMPTY_BLOB)) {
            throw new EntityNotFoundException();
        }
        Object entity = serializer.deserialize(value);
        assert Entities.isEntity(entity);
        return entity;
    }

    public Blob forth(Object entity) {
        if (entity == null) {
            return null;
        }
        assert Entities.isEntity(entity);
        return serializer.serialize(entity);
    }
}
