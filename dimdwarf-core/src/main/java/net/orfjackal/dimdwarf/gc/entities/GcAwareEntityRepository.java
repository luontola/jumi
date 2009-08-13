// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.gc.entities;

import com.google.inject.Inject;
import net.orfjackal.dimdwarf.api.EntityId;
import net.orfjackal.dimdwarf.db.Blob;
import net.orfjackal.dimdwarf.entities.*;
import net.orfjackal.dimdwarf.entities.dao.EntityDao;
import net.orfjackal.dimdwarf.gc.MutatorListener;
import net.orfjackal.dimdwarf.scopes.TaskScoped;
import net.orfjackal.dimdwarf.serial.*;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;

/**
 * @author Esko Luontola
 * @since 1.9.2008
 */
@TaskScoped
@NotThreadSafe
public class GcAwareEntityRepository implements EntityRepository {

    private final EntityDao entities;
    private final ObjectSerializer serializer;
    private final MutatorListener<EntityId> listener;

    private final Map<EntityId, Set<EntityId>> referencesOnRead = new HashMap<EntityId, Set<EntityId>>();

    @Inject
    public GcAwareEntityRepository(EntityDao entities,
                                   ObjectSerializer serializer,
                                   MutatorListener<EntityId> listener) {
        this.entities = entities;
        this.serializer = serializer;
        this.listener = listener;
    }

    public boolean exists(EntityId id) {
        return entities.exists(id);
    }

    public Object read(EntityId id) {
        DeserializationResult oldData = readFromDatabase(id);
        cacheReferencesOnRead(id, oldData);
        return oldData.getDeserializedObject();
    }

    private DeserializationResult readFromDatabase(EntityId id) {
        Blob bytes = entities.read(id);
        if (bytes.equals(Blob.EMPTY_BLOB)) {
            throw new EntityNotFoundException("id=" + id);
        }
        return serializer.deserialize(bytes);
    }

    private void cacheReferencesOnRead(EntityId id, DeserializationResult oldData) {
        Set<EntityId> oldReferences = getReferencedEntities(oldData);
        referencesOnRead.put(id, oldReferences);
    }

    private static Set<EntityId> getReferencedEntities(ResultWithMetadata result) {
        List<EntityId> possibleDuplicates = result.getMetadata(EntityReferenceListener.class);
        return new HashSet<EntityId>(possibleDuplicates);
    }

    public void update(EntityId id, Object entity) {
        SerializationResult newData = serializer.serialize(entity);
        if (hasBeenModified(id, newData)) {
            entities.update(id, newData.getSerializedBytes());
            fireEntityUpdated(id, newData);
        }
    }

    private boolean hasBeenModified(EntityId id, SerializationResult newData) {
        Blob oldBytes = entities.read(id);
        Blob newBytes = newData.getSerializedBytes();
        return !oldBytes.equals(newBytes);
    }

    private void fireEntityUpdated(EntityId id, SerializationResult newData) {
        Set<EntityId> newReferences = getReferencedEntities(newData);
        Set<EntityId> oldReferences = referencesOnRead.remove(id);
        if (oldReferences == null) {
            oldReferences = Collections.emptySet();
            fireEntityCreated(id);
        }
        fireReferencesRemoved(id, newReferences, oldReferences);
        fireReferencesCreated(id, newReferences, oldReferences);
    }

    private void fireEntityCreated(EntityId id) {
        listener.onNodeCreated(id);
    }

    private void fireReferencesRemoved(EntityId id, Set<EntityId> newReferences, Set<EntityId> oldReferences) {
        for (EntityId targetId : oldReferences) {
            if (!newReferences.contains(targetId)) {
                listener.onReferenceRemoved(id, targetId);
            }
        }
    }

    private void fireReferencesCreated(EntityId id, Set<EntityId> newReferences, Set<EntityId> oldReferences) {
        for (EntityId targetId : newReferences) {
            if (!oldReferences.contains(targetId)) {
                listener.onReferenceCreated(id, targetId);
            }
        }
    }

    public void delete(EntityId id) {
        DeserializationResult oldData = readFromDatabase(id);
        entities.delete(id);
        fireEntityDeleted(id, oldData);
    }

    private void fireEntityDeleted(EntityId id, DeserializationResult oldData) {
        Set<EntityId> newReferences = Collections.emptySet();
        Set<EntityId> oldReferences = getReferencedEntities(oldData);
        fireReferencesRemoved(id, newReferences, oldReferences);
    }

    public EntityId firstKey() {
        return entities.firstKey();
    }

    public EntityId nextKeyAfter(EntityId currentKey) {
        return entities.nextKeyAfter(currentKey);
    }
}
