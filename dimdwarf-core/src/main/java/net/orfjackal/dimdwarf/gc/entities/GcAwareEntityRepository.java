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
import net.orfjackal.dimdwarf.db.Blob;
import net.orfjackal.dimdwarf.entities.*;
import net.orfjackal.dimdwarf.entities.dao.EntityDao;
import net.orfjackal.dimdwarf.gc.MutatorListener;
import net.orfjackal.dimdwarf.scopes.TaskScoped;
import net.orfjackal.dimdwarf.serial.*;

import javax.annotation.concurrent.NotThreadSafe;
import java.math.BigInteger;
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
    private final MutatorListener<BigInteger> listener;

    private final Map<BigInteger, Set<BigInteger>> referencesOnRead = new HashMap<BigInteger, Set<BigInteger>>();

    @Inject
    public GcAwareEntityRepository(EntityDao entities,
                                   ObjectSerializer serializer,
                                   MutatorListener<BigInteger> listener) {
        this.entities = entities;
        this.serializer = serializer;
        this.listener = listener;
    }

    public boolean exists(BigInteger id) {
        return entities.exists(id);
    }

    public Object read(BigInteger id) {
        DeserializationResult oldData = readFromDatabase(id);
        cacheReferencesOnRead(id, oldData);
        return oldData.getDeserializedObject();
    }

    private DeserializationResult readFromDatabase(BigInteger id) {
        Blob bytes = entities.read(id);
        if (bytes.equals(Blob.EMPTY_BLOB)) {
            throw new EntityNotFoundException("id=" + id);
        }
        return serializer.deserialize(bytes);
    }

    private void cacheReferencesOnRead(BigInteger id, DeserializationResult oldData) {
        Set<BigInteger> oldReferences = getReferencedEntities(oldData);
        referencesOnRead.put(id, oldReferences);
    }

    private static Set<BigInteger> getReferencedEntities(ResultWithMetadata result) {
        List<BigInteger> possibleDuplicates = result.getMetadata(EntityReferenceListener.class);
        return new HashSet<BigInteger>(possibleDuplicates);
    }

    public void update(BigInteger id, Object entity) {
        SerializationResult newData = serializer.serialize(entity);
        if (hasBeenModified(id, newData)) {
            entities.update(id, newData.getSerializedBytes());
            fireEntityUpdated(id, newData);
        }
    }

    private boolean hasBeenModified(BigInteger id, SerializationResult newData) {
        Blob oldBytes = entities.read(id);
        Blob newBytes = newData.getSerializedBytes();
        return !oldBytes.equals(newBytes);
    }

    private void fireEntityUpdated(BigInteger id, SerializationResult newData) {
        Set<BigInteger> newReferences = getReferencedEntities(newData);
        Set<BigInteger> oldReferences = referencesOnRead.remove(id);
        if (oldReferences == null) {
            oldReferences = Collections.emptySet();
            fireEntityCreated(id);
        }
        fireReferencesRemoved(id, newReferences, oldReferences);
        fireReferencesCreated(id, newReferences, oldReferences);
    }

    private void fireEntityCreated(BigInteger id) {
        listener.onNodeCreated(id);
    }

    private void fireReferencesRemoved(BigInteger id, Set<BigInteger> newReferences, Set<BigInteger> oldReferences) {
        for (BigInteger targetId : oldReferences) {
            if (!newReferences.contains(targetId)) {
                listener.onReferenceRemoved(id, targetId);
            }
        }
    }

    private void fireReferencesCreated(BigInteger id, Set<BigInteger> newReferences, Set<BigInteger> oldReferences) {
        for (BigInteger targetId : newReferences) {
            if (!oldReferences.contains(targetId)) {
                listener.onReferenceCreated(id, targetId);
            }
        }
    }

    public void delete(BigInteger id) {
        DeserializationResult oldData = readFromDatabase(id);
        entities.delete(id);
        fireEntityDeleted(id, oldData);
    }

    private void fireEntityDeleted(BigInteger id, DeserializationResult oldData) {
        Set<BigInteger> newReferences = Collections.emptySet();
        Set<BigInteger> oldReferences = getReferencedEntities(oldData);
        fireReferencesRemoved(id, newReferences, oldReferences);
    }

    public BigInteger firstKey() {
        return entities.firstKey();
    }

    public BigInteger nextKeyAfter(BigInteger currentKey) {
        return entities.nextKeyAfter(currentKey);
    }
}
