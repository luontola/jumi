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

import net.orfjackal.dimdwarf.api.internal.EntityReference;
import net.orfjackal.dimdwarf.db.Blob;
import net.orfjackal.dimdwarf.entities.tref.TransparentReferenceImpl;
import net.orfjackal.dimdwarf.serial.*;
import net.orfjackal.dimdwarf.util.SerializableIterable;

import java.math.BigInteger;
import java.util.*;

/**
 * @author Esko Luontola
 * @since 11.12.2008
 */
public class EntityReferenceUtil {
    // TODO: invent a better name (or integrate this functionality to ObjectSerializer using listeners and better return values)

    public Iterable<BigInteger> getReferencedEntityIds(Blob entity) {
        EntityReferenceListener collectReferences = new EntityReferenceListener();
        DeserializationResult di = new ObjectSerializerImpl(
                new SerializationListener[]{collectReferences},
                new SerializationReplacer[]{new TransparentReferenceDiscarder()}
        ).deserialize(entity);
        return new SerializableIterable<BigInteger>(collectReferences.getReferences());
    }

    private static class EntityReferenceListener extends SerializationAdapter {

        private final List<BigInteger> references = new ArrayList<BigInteger>();

        @Override
        public void afterDeserialize(Object obj, MetadataBuilder meta) {
            if (obj instanceof EntityReference) {
                EntityReference<?> ref = (EntityReference<?>) obj;
                references.add(ref.getEntityId());
            }
        }

        public List<BigInteger> getReferences() {
            return new ArrayList<BigInteger>(references);
        }
    }

    private static class TransparentReferenceDiscarder extends SerializationReplacerAdapter {

        @Override
        public Object resolveDeserialized(Object obj, MetadataBuilder meta) {
            if (obj instanceof TransparentReferenceImpl) {
                return null;
            }
            return obj;
        }
    }
}
