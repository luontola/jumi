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

package net.orfjackal.dimdwarf.entities.tref;

import com.google.inject.Inject;
import net.orfjackal.dimdwarf.api.internal.*;
import net.orfjackal.dimdwarf.serial.*;

import javax.annotation.concurrent.Immutable;

/**
 * @author Esko Luontola
 * @since 5.9.2008
 */
@Immutable
public class ReplaceEntitiesWithTransparentReferences implements SerializationReplacer {

    private final TransparentReferenceFactory factory;

    @Inject
    public ReplaceEntitiesWithTransparentReferences(TransparentReferenceFactory factory) {
        this.factory = factory;
    }

    public Object replaceSerialized(Object rootObject, Object obj, MetadataBuilder meta) {
        if (obj != rootObject && Entities.isEntity(obj)) {
            return createTransparentReferenceForSerialization(obj);
        }
        return obj;
    }

    private Object createTransparentReferenceForSerialization(Object entity) {
        TransparentReference notSerializableProxy = factory.createTransparentReference(entity);
        // The call to writeReplace() is needed because ObjectOutputStream#replaceObject does not check
        // whether the returned objects have a writeReplace() method.
        return notSerializableProxy.writeReplace();
    }

    public Object resolveDeserialized(Object obj, MetadataBuilder meta) {
        if (obj instanceof TransparentReferenceImpl) {
            return factory.newProxy((TransparentReferenceImpl) obj);
        }
        return obj;
    }
}
