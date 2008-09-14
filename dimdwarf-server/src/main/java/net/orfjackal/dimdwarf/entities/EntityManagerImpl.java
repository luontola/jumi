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
import net.orfjackal.dimdwarf.api.impl.Entities;
import net.orfjackal.dimdwarf.api.impl.EntityReference;
import net.orfjackal.dimdwarf.api.impl.IEntity;
import net.orfjackal.dimdwarf.scopes.TaskScoped;
import net.orfjackal.dimdwarf.tx.Transaction;
import net.orfjackal.dimdwarf.tx.TransactionListener;

import java.math.BigInteger;
import java.util.*;

/**
 * This class is NOT thread-safe.
 *
 * @author Esko Luontola
 * @since 25.8.2008
 */
@TaskScoped
public class EntityManagerImpl implements ReferenceFactory, EntityLoader, TransactionListener {

    private final Map<IEntity, EntityReference<?>> entities = new IdentityHashMap<IEntity, EntityReference<?>>();
    private final Map<BigInteger, IEntity> entitiesById = new HashMap<BigInteger, IEntity>();
    private final Queue<IEntity> flushQueue = new ArrayDeque<IEntity>();
    private final EntityIdFactory idFactory;
    private final EntityStorage storage;
    private State state = State.ACTIVE;

    @Inject
    public EntityManagerImpl(EntityIdFactory idFactory, EntityStorage storage, Transaction tx) {
        this.idFactory = idFactory;
        this.storage = storage;
        tx.addTransactionListener(this);
    }

    public int getRegisteredEntities() {
        return entities.size();
    }

    @SuppressWarnings({"unchecked"})
    public <T> EntityReference<T> createReference(T obj) {
        checkStateIs(State.ACTIVE, State.FLUSHING);
        if (!Entities.isEntity(obj)) {
            throw new IllegalArgumentException("Not an entity: " + obj);
        }
        IEntity entity = (IEntity) obj;
        EntityReference<T> ref = (EntityReference<T>) entities.get(entity);
        if (ref == null) {
            ref = new EntityReferenceImpl<T>(idFactory.newId(), obj);
            register(entity, ref);
        }
        return ref;
    }

    public Object loadEntity(BigInteger id) {
        checkStateIs(State.ACTIVE);
        return loadAndRegister(id, null);
    }

    @SuppressWarnings({"unchecked"})
    public <T> T loadEntity(EntityReference<T> ref) {
        checkStateIs(State.ACTIVE);
        return (T) loadAndRegister(ref.getId(), ref);
    }

    private IEntity loadAndRegister(BigInteger id, EntityReference<?> ref) {
        IEntity entity = entitiesById.get(id);
        if (entity == null) {
            entity = (IEntity) storage.read(id);
            if (ref == null) {
                ref = new EntityReferenceImpl<Object>(id, entity);
            }
            register(entity, ref);
        }
        return entity;
    }

    private void register(IEntity entity, EntityReference<?> ref) {
        if (state == State.FLUSHING) {
            flushQueue.add(entity);
        }
        entitiesById.put(ref.getId(), entity);
        EntityReference<?> previous =
                entities.put(entity, ref);
        assert previous == null : "Registered an entity twise: " + entity + ", " + ref;
    }

    public BigInteger firstKey() {
        checkStateIs(State.ACTIVE);
        return storage.firstKey();
    }

    public BigInteger nextKeyAfter(BigInteger currentKey) {
        checkStateIs(State.ACTIVE);
        return storage.nextKeyAfter(currentKey);
    }

    public void transactionWillDeactivate(Transaction tx) {
        flushAllEntities();
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
        IEntity entity;
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
