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

import net.orfjackal.dimdwarf.api.Entity;
import net.orfjackal.dimdwarf.api.internal.EntityReference;

import java.math.BigInteger;
import java.util.*;

/**
 * This class is NOT thread-safe.
 *
 * @author Esko Luontola
 * @since 25.8.2008
 */
public class EntityManagerImpl implements EntityLoader, EntityManager {

    private final Map<Entity, EntityReference<?>> entities = new IdentityHashMap<Entity, EntityReference<?>>();
    private final Map<EntityReference<?>, Entity> cache = new HashMap<EntityReference<?>, Entity>();
    private final Queue<Entity> flushQueue = new ArrayDeque<Entity>();
    private final EntityIdFactory idFactory;
    private final EntityStorage storage;
    private State state = State.ACTIVE;

    public EntityManagerImpl(EntityIdFactory idFactory, EntityStorage storage) {
        this.idFactory = idFactory;
        this.storage = storage;
    }

    public int getRegisteredEntities() {
        return entities.size();
    }

    public <T> EntityReference<T> createReference(T entity) {
        checkStateIs(State.ACTIVE, State.FLUSHING);
        EntityReference<T> ref = (EntityReference<T>) entities.get((Entity) entity);
        if (ref == null) {
            ref = new EntityReferenceImpl<T>(idFactory.newId(), entity);
            register((Entity) entity, ref);
        }
        return ref;
    }

    public <T> T loadEntity(EntityReference<T> ref) {
        checkStateIs(State.ACTIVE);
        Entity entity = cache.get(ref);
        if (entity == null) {
            entity = storage.read(ref.getId());
            register(entity, ref);
        }
        return (T) entity;
    }

    private void register(Entity entity, EntityReference<?> ref) {
        if (state == State.FLUSHING) {
            flushQueue.add(entity);
        }
        cache.put(ref, entity);
        EntityReference<?> previous = entities.put((Entity) entity, ref);
        assert previous == null : "Registered an entity twise: " + entity + ", " + ref;
    }

    public void flushAllEntities() {
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
        Entity entity;
        while ((entity = flushQueue.poll()) != null) {
            BigInteger id = entities.get(entity).getId();
            storage.update(id, entity);
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
