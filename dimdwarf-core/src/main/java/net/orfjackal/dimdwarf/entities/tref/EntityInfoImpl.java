// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities.tref;

import com.google.inject.Inject;
import net.orfjackal.dimdwarf.api.EntityInfo;
import net.orfjackal.dimdwarf.api.internal.*;
import net.orfjackal.dimdwarf.entities.EntityManager;

import javax.annotation.concurrent.Immutable;
import java.math.BigInteger;

/**
 * @author Esko Luontola
 * @since 2.11.2008
 */
@Immutable
public class EntityInfoImpl implements EntityInfo {

    private final EntityManager entityManager;

    @Inject
    public EntityInfoImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public BigInteger getEntityId(Object entity) {
        if (Entities.isEntity(entity)) {
            return getIdFromEntity(entity);
        }
        if (Entities.isTransparentReference(entity)) {
            return getIdFromProxy(entity);
        }
        throw new IllegalArgumentException("Not an entity: " + entity);
    }

    private BigInteger getIdFromEntity(Object entity) {
        return entityManager.getEntityId((EntityObject) entity);
    }

    private BigInteger getIdFromProxy(Object proxy) {
        TransparentReference tref = (TransparentReference) proxy;
        return tref.getEntityReference$TREF().getEntityId();
    }
}
