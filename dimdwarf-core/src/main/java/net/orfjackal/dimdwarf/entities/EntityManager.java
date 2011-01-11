// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import net.orfjackal.dimdwarf.api.EntityId;
import net.orfjackal.dimdwarf.api.internal.*;
import net.orfjackal.dimdwarf.tasks.TaskScoped;
import org.jetbrains.annotations.TestOnly;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;
import java.util.*;

@TaskScoped
@NotThreadSafe
public class EntityManager implements AllEntities, EntitiesLoadedInMemory {

    private final EntityIdFactory idFactory;
    private final EntitiesPersistedInDatabase persistedEntities;
    private final EntityApi entityApi;

    private final Map<EntityObject, EntityId> entities = new IdentityHashMap<EntityObject, EntityId>();
    private final Map<EntityId, EntityObject> entitiesById = new HashMap<EntityId, EntityObject>();
    private final Queue<EntityObject> flushQueue = new ArrayDeque<EntityObject>();
    private State state = State.ACTIVE;

    @Inject
    public EntityManager(EntityIdFactory idFactory, EntitiesPersistedInDatabase persistedEntities, EntityApi entityApi) {
        this.idFactory = idFactory;
        this.persistedEntities = persistedEntities;
        this.entityApi = entityApi;
    }

    @TestOnly
    int getRegisteredEntities() {
        return entities.size();
    }

    public EntityId getEntityId(EntityObject entity) {
        checkStateIs(State.ACTIVE, State.FLUSHING);
        checkIsEntity(entity);
        EntityId id = getIdOfLoadedEntity(entity);
        if (id == null) {
            id = createIdForNewEntity(entity);
        }
        return id;
    }

    private void checkIsEntity(EntityObject entity) {
        if (!entityApi.isEntity(entity)) {
            throw new IllegalArgumentException("Not an entity: " + entity);
        }
    }

    private EntityId getIdOfLoadedEntity(EntityObject entity) {
        return entities.get(entity);
    }

    private EntityId createIdForNewEntity(EntityObject entity) {
        EntityId id = idFactory.newId();
        register(entity, id);
        return id;
    }

    public EntityObject getEntityById(EntityId id) {
        checkStateIs(State.ACTIVE);
        EntityObject entity = getLoadedEntity(id);
        if (entity == null) {
            entity = loadEntityFromDatabase(id);
        }
        return entity;
    }

    @Nullable
    private EntityObject getLoadedEntity(EntityId id) {
        return entitiesById.get(id);
    }

    private EntityObject loadEntityFromDatabase(EntityId id) {
        EntityObject entity = (EntityObject) persistedEntities.read(id);
        register(entity, id);
        return entity;
    }

    private void register(EntityObject entity, EntityId id) {
        if (state == State.FLUSHING) {
            flushQueue.add(entity);
        }
        EntityId prevIdOfSameEntity = entities.put(entity, id);
        EntityObject prevEntityWithSameId = entitiesById.put(id, entity);
        assert prevIdOfSameEntity == null && prevEntityWithSameId == null : ""
                + "Registered an entity twise: " + entity + ", " + id
                + " (Previous was: " + prevEntityWithSameId + ", " + prevIdOfSameEntity + ")";
    }

    public void flushToDatabase() {
        beginFlush();
        flush();
        endFlush();
    }

    private void beginFlush() {
        checkStateIs(State.ACTIVE);
        state = State.FLUSHING;
        assert flushQueue.isEmpty();
        flushQueue.addAll(entities.keySet());
    }

    private void flush() {
        EntityObject entity;
        while ((entity = flushQueue.poll()) != null) {
            EntityId id = entities.get(entity);
            persistedEntities.update(id, entity);
        }
    }

    private void endFlush() {
        checkStateIs(State.FLUSHING);
        state = State.CLOSED;
        assert flushQueue.isEmpty();
    }

    private void checkStateIs(State... expectedStates) {
        for (State expected : expectedStates) {
            if (state == expected) {
                return;
            }
        }
        throw new IllegalStateException("Expected state " + Arrays.toString(expectedStates) + " but was " + state);
    }

    private enum State {
        ACTIVE, FLUSHING, CLOSED
    }
}
