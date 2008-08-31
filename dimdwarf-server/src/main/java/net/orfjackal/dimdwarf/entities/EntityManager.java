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
 * @author Esko Luontola
 * @since 25.8.2008
 */
public class EntityManager implements EntityLoader {

    private final Map<Entity, EntityReference<?>> entities = new IdentityHashMap<Entity, EntityReference<?>>();
    private final Map<EntityReference<?>, Entity> cache = new HashMap<EntityReference<?>, Entity>();
    private final EntityIdFactory idFactory;
    private final EntityStorage storage;
    private State state = State.ACTIVE;
    private final Queue<Entity> flushQueue = new LinkedList<Entity>();

    public EntityManager(EntityIdFactory idFactory, EntityStorage storage) {
        this.idFactory = idFactory;
        this.storage = storage;
    }

    public int getRegisteredEntities() {
        return entities.size();
    }

    public <T> EntityReference<T> createReference(T entity) {
        if (state == State.FLUSHING) {
            flushQueue.add((Entity) entity);
        }
        EntityReferenceImpl<T> ref = (EntityReferenceImpl<T>) entities.get((Entity) entity);
        if (ref == null) {
            ref = new EntityReferenceImpl<T>(idFactory.newId(), entity);
            EntityReference<?> prev = entities.put((Entity) entity, ref);
            assert prev == null;
            // There should be no need to put the entity and reference to 'cache' here, because:
            // - If the entity was loaded from database, it was already put there by 'loadEntity'.
            // - If the object was just created, it will not be in the database during this task,
            //   so 'loadEntity' will not be called for a reference pointing to it.
        }
        return ref;
    }

    public <T> T loadEntity(EntityReference<T> ref) {
        Entity entity = cache.get(ref);
        if (entity == null) {
            entity = storage.read(ref.getId());
            cache.put(ref, entity);
            EntityReference<?> prev = entities.put(entity, (EntityReferenceImpl<?>) ref);
            assert prev == null;
        }
        return (T) entity;
    }

    public void flushAllEntities() {
        state = State.FLUSHING;
        flushQueue.addAll(entities.keySet());

        Entity entity;
        while ((entity = flushQueue.poll()) != null) {
            BigInteger id = entities.get(entity).getId();
            storage.update(id, entity);
        }
    }

    private enum State {
        ACTIVE, FLUSHING, CLOSED
    }
}
