// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import com.google.inject.Inject;
import net.orfjackal.dimdwarf.api.internal.EntityReference;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.*;
import java.math.BigInteger;

/**
 * @author Esko Luontola
 * @since 25.8.2008
 */
@NotThreadSafe
public class EntityReferenceImpl<T> implements EntityReference<T>, Externalizable {
    private static final long serialVersionUID = 1L;

    private BigInteger id;
    @Nullable private transient T entity;
    @Nullable private transient EntityManager entityManager;

    public EntityReferenceImpl(BigInteger id, T entity) {
        assert id != null;
        assert entity != null;
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
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public T get() {
        if (entity == null) {
            entity = (T) entityManager.getEntityById(id);
        }
        return entity;
    }

    public BigInteger getEntityId() {
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
