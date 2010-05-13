// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import com.google.inject.Inject;
import net.orfjackal.dimdwarf.api.EntityId;
import net.orfjackal.dimdwarf.api.internal.EntityReference;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.Serializable;

@NotThreadSafe
public class EntityReferenceImpl<T> implements EntityReference<T>, Serializable {
    private static final long serialVersionUID = 1L;

    private final EntityId id;
    @Nullable private transient T entity;
    @Nullable private transient AllEntities entities;

    public EntityReferenceImpl(EntityId id, T entity) {
        assert id != null;
        assert entity != null;
        this.id = id;
        this.entity = entity;
    }

    /**
     * Needs to be injected when the reference is deserialized. No need to inject when
     * the reference is created directly, because then the entity is already cached locally.
     */
    @Inject
    public void setEntityLocator(AllEntities entities) {
        this.entities = entities;
    }

    public T get() {
        if (entity == null) {
            entity = (T) entities.getEntityById(id);
        }
        return entity;
    }

    public EntityId getEntityId() {
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
