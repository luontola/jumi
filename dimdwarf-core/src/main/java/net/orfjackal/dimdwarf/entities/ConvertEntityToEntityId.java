// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import com.google.inject.Inject;
import net.orfjackal.dimdwarf.api.*;
import net.orfjackal.dimdwarf.db.Converter;

import javax.annotation.concurrent.Immutable;

/**
 * @author Esko Luontola
 * @since 12.9.2008
 */
@Immutable
public class ConvertEntityToEntityId implements Converter<Object, EntityId> {

    private final EntityManager entityManager;
    private final EntityInfo entityInfo;

    @Inject
    public ConvertEntityToEntityId(EntityManager entityManager, EntityInfo entityInfo) {
        this.entityManager = entityManager;
        this.entityInfo = entityInfo;
    }

    public Object back(EntityId id) {
        if (id == null) {
            return null;
        }
        return entityManager.getEntityById(id);
    }

    public EntityId forth(Object entity) {
        if (entity == null) {
            return null;
        }
        // EntityInfo must be used instead of EntityManager, because the object
        // could be a transparent reference proxy, and EntityManager does not
        // know how to handle transparent references.
        return entityInfo.getEntityId(entity);
    }
}
