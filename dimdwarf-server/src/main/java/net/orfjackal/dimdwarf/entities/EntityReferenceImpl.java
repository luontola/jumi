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
import net.orfjackal.dimdwarf.api.impl.EntityReference;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.math.BigInteger;

/**
 * This class is NOT thread-safe.
 *
 * @author Esko Luontola
 * @since 25.8.2008
 */
public class EntityReferenceImpl<T> implements EntityReference<T>, Externalizable {
    private static final long serialVersionUID = 1L;

    private BigInteger id;
    private transient T entity;
    private transient EntityLoader entityLoader;

    public EntityReferenceImpl(BigInteger id, T entity) {
        this.id = id;
        this.entity = entity;
    }

    public EntityReferenceImpl() {
        // default constructor is required by Externalizable
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        byte[] bytes = id.toByteArray();
        out.writeObject(bytes);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        byte[] bytes = (byte[]) in.readObject();
        id = new BigInteger(bytes);
    }

    /**
     * Needs to be injected when the reference is deserialized. No need to inject when
     * the reference is created, because then the entity is already cached locally.
     */
    @Inject
    public void setEntityLoader(EntityLoader loader) {
        this.entityLoader = loader;
    }

    public T get() {
        if (entity == null) {
            entity = entityLoader.loadEntity(this);
        }
        return entity;
    }

    public BigInteger getId() {
        return id;
    }

    public int hashCode() {
        return id.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj instanceof EntityReferenceImpl) {
            EntityReferenceImpl<?> other = (EntityReferenceImpl<?>) obj;
            return id.equals(other.id);
        }
        return false;
    }

    public String toString() {
        return getClass().getSimpleName() + "[id=" + id + "]";
    }
}
