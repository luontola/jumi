// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

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
        if (entities.exists(node)) {
            return entities.readMetadata(node, metaKey).getByteArray();
        } else {
            return new byte[0];
        }
    }

    public void setMetadata(BigInteger node, String metaKey, byte[] metaValue) {
        entities.updateMetadata(node, metaKey, Blob.fromBytes(metaValue));
    }


    private static class AllNodesIterator implements Iterator<BigInteger>, Serializable {
        private static final long serialVersionUID = 1L;

        private final DatabaseKeyIterator<BigInteger> iterator = new DatabaseKeyIterator<BigInteger>();

        public AllNodesIterator(EntityDao entities) {
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

        private final DatabaseKeyIterator<String> iterator = new DatabaseKeyIterator<String>();
        private transient BindingDao bindings;

        public RootNodesIterator(BindingDao bindings) {
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
