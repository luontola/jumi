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
import net.orfjackal.dimdwarf.api.internal.*;
import net.orfjackal.dimdwarf.scopes.TaskScoped;
import org.jetbrains.annotations.TestOnly;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.math.BigInteger;
import java.util.*;

/**
 * @author Esko Luontola
 * @since 25.8.2008
 */
@TaskScoped
@NotThreadSafe
public class EntityManagerImpl implements EntityManager {

    private final EntityIdFactory idFactory;
    private final EntityRepository repository;

    private final Map<EntityObject, BigInteger> entities = new IdentityHashMap<EntityObject, BigInteger>();
    private final Map<BigInteger, EntityObject> entitiesById = new HashMap<BigInteger, EntityObject>();
    private final Queue<EntityObject> flushQueue = new ArrayDeque<EntityObject>();
    private State state = State.ACTIVE;

    @Inject
    public EntityManagerImpl(EntityIdFactory idFactory, EntityRepository repository) {
        this.idFactory = idFactory;
        this.repository = repository;
    }

    @TestOnly
    int getRegisteredEntities() {
        return entities.size();
    }

    public BigInteger getEntityId(EntityObject entity) {
        checkStateIs(State.ACTIVE, State.FLUSHING);
        checkIsEntity(entity);
        BigInteger id = getIdOfLoadedEntity(entity);
        if (id == null) {
            id = createIdForNewEntity(entity);
        }
        return id;
    }

    private static void checkIsEntity(EntityObject entity) {
        if (!Entities.isEntity(entity)) {
            throw new IllegalArgumentException("Not an entity: " + entity);
        }
    }

    private BigInteger getIdOfLoadedEntity(EntityObject entity) {
        return entities.get(entity);
    }

    private BigInteger createIdForNewEntity(EntityObject entity) {
        BigInteger id = idFactory.newId();
        register(entity, id);
        return id;
    }

    public EntityObject getEntityById(BigInteger id) {
        checkStateIs(State.ACTIVE);
        EntityObject entity = getLoadedEntity(id);
        if (entity == null) {
            entity = loadEntityFromDatabase(id);
        }
        return entity;
    }

    @Nullable
    private EntityObject getLoadedEntity(BigInteger id) {
        return entitiesById.get(id);
    }

    private EntityObject loadEntityFromDatabase(BigInteger id) {
        EntityObject entity = (EntityObject) repository.read(id);
        register(entity, id);
        return entity;
    }

    private void register(EntityObject entity, BigInteger id) {
        if (state == State.FLUSHING) {
            flushQueue.add(entity);
        }
        BigInteger prevIdOfSameEntity = entities.put(entity, id);
        EntityObject prevEntityWithSameId = entitiesById.put(id, entity);
        assert prevIdOfSameEntity == null && prevEntityWithSameId == null : ""
                + "Registered an entity twise: " + entity + ", " + id
                + " (Previous was: " + prevEntityWithSameId + ", " + prevIdOfSameEntity + ")";
    }

    public BigInteger firstKey() {
        checkStateIs(State.ACTIVE);
        return repository.firstKey();
    }

    public BigInteger nextKeyAfter(BigInteger currentKey) {
        checkStateIs(State.ACTIVE);
        return repository.nextKeyAfter(currentKey);
    }

    /**
     * Must be called before transaction deactivates, or the changes to entities will not be persisted.
     */
    public void flushAllEntitiesToDatabase() {
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
            BigInteger id = entities.get(entity);
            repository.update(id, entity);
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
