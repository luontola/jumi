// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.gc.entities;

import com.google.inject.Inject;
import net.orfjackal.dimdwarf.api.EntityId;
import net.orfjackal.dimdwarf.db.*;
import net.orfjackal.dimdwarf.entities.dao.*;
import net.orfjackal.dimdwarf.gc.Graph;
import net.orfjackal.dimdwarf.serial.*;
import net.orfjackal.dimdwarf.util.SerializableIterable;

import java.io.Serializable;
import java.util.*;

/**
 * @author Esko Luontola
 * @since 30.11.2008
 */
public class EntityGraph implements Graph<EntityId> {

    private final EntityDao entities;
    private final BindingDao bindings;
    private final ObjectSerializer serializer;

    @Inject
    public EntityGraph(EntityDao entities, BindingDao bindings, ObjectSerializer serializer) {
        this.entities = entities;
        this.bindings = bindings;
        this.serializer = serializer;
    }

    public Iterable<EntityId> getAllNodes() {
        return new Iterable<EntityId>() {
            public Iterator<EntityId> iterator() {
                return new AllNodesIterator(entities);
            }
        };
    }

    public Iterable<EntityId> getRootNodes() {
        return new Iterable<EntityId>() {
            public Iterator<EntityId> iterator() {
                return new RootNodesIterator(bindings);
            }
        };
    }

    public Iterable<EntityId> getConnectedNodesOf(EntityId node) {
        Blob entity = entities.read(node);
        List<EntityId> ids = getReferencedEntityIds(entity);
        return new SerializableIterable<EntityId>(ids);
    }

    private List<EntityId> getReferencedEntityIds(Blob entity) {
        DeserializationResult result = serializer.deserialize(entity);
        return result.getMetadata(EntityReferenceListener.class);
    }

    public void removeNode(EntityId node) {
        entities.delete(node);
    }

    public byte[] getMetadata(EntityId node, String metaKey) {
        if (entities.exists(node)) {
            return entities.readMetadata(node, metaKey).getByteArray();
        } else {
            return new byte[0];
        }
    }

    public void setMetadata(EntityId node, String metaKey, byte[] metaValue) {
        entities.updateMetadata(node, metaKey, Blob.fromBytes(metaValue));
    }


    private static class AllNodesIterator implements Iterator<EntityId>, Serializable {
        private static final long serialVersionUID = 1L;

        private final DatabaseKeyIterator<EntityId> iterator = new DatabaseKeyIterator<EntityId>();

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

        public EntityId next() {
            return iterator.next();
        }

        public void remove() {
            iterator.remove();
        }
    }

    private static class RootNodesIterator implements Iterator<EntityId>, Serializable {
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

        public EntityId next() {
            String binding = iterator.next();
            return bindings.read(binding);
        }

        public void remove() {
            iterator.remove();
        }
    }
}
