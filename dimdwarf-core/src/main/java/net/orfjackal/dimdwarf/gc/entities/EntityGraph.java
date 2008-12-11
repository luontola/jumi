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
import net.orfjackal.dimdwarf.db.*;
import net.orfjackal.dimdwarf.entities.dao.*;
import net.orfjackal.dimdwarf.gc.Graph;
import net.orfjackal.dimdwarf.serial.*;
import net.orfjackal.dimdwarf.util.SerializableIterable;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/**
 * @author Esko Luontola
 * @since 30.11.2008
 */
public class EntityGraph implements Graph<BigInteger> {

    private final EntityDao entities;
    private final BindingDao bindings;
    private final ObjectSerializer serializer;

    @Inject
    public EntityGraph(EntityDao entities, BindingDao bindings, ObjectSerializer serializer) {
        this.entities = entities;
        this.bindings = bindings;
        this.serializer = serializer;
    }

    public Iterable<BigInteger> getAllNodes() {
        return new Iterable<BigInteger>() {
            public Iterator<BigInteger> iterator() {
                return new AllNodesIterator(entities);
            }
        };
    }

    public Iterable<BigInteger> getRootNodes() {
        return new Iterable<BigInteger>() {
            public Iterator<BigInteger> iterator() {
                return new RootNodesIterator(bindings);
            }
        };
    }

    public Iterable<BigInteger> getConnectedNodesOf(BigInteger node) {
        Blob entity = entities.read(node);
        List<BigInteger> ids = getReferencedEntityIds(entity);
        return new SerializableIterable<BigInteger>(ids);
    }

    private List<BigInteger> getReferencedEntityIds(Blob entity) {
        DeserializationResult result = serializer.deserialize(entity);
        return result.getMetadata(EntityReferenceListener.class);
    }

    public void removeNode(BigInteger node) {
        entities.delete(node);
    }

    public byte[] getMetadata(BigInteger node, String metaKey) {
        return entities.readMetadata(node, metaKey).getByteArray();
    }

    public void setMetadata(BigInteger node, String metaKey, byte[] metaValue) {
        entities.updateMetadata(node, metaKey, Blob.fromBytes(metaValue));
    }


    private static class AllNodesIterator implements Iterator<BigInteger>, Serializable {
        private static final long serialVersionUID = 1L;

        private final DatabaseKeyIterator<BigInteger> iterator;

        public AllNodesIterator(EntityDao entities) {
            iterator = new DatabaseKeyIterator<BigInteger>(entities.firstKey());
            setEntityDao(entities);
        }

        @Inject
        public void setEntityDao(EntityDao entities) {
            iterator.setTable(entities);
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public BigInteger next() {
            return iterator.next();
        }

        public void remove() {
            iterator.remove();
        }
    }

    private static class RootNodesIterator implements Iterator<BigInteger>, Serializable {
        private static final long serialVersionUID = 1L;

        private final DatabaseKeyIterator<String> iterator;
        private transient BindingDao bindings;

        public RootNodesIterator(BindingDao bindings) {
            iterator = new DatabaseKeyIterator<String>(bindings.firstKey());
            setBindingDao(bindings);
        }

        @Inject
        public void setBindingDao(BindingDao bindings) {
            this.bindings = bindings;
            iterator.setTable(bindings);
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public BigInteger next() {
            String binding = iterator.next();
            return bindings.read(binding);
        }

        public void remove() {
            iterator.remove();
        }
    }
}
