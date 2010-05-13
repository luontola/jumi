// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import com.google.inject.Inject;
import net.orfjackal.dimdwarf.api.*;
import net.orfjackal.dimdwarf.db.Converter;

import javax.annotation.concurrent.Immutable;

@Immutable
public class ConvertEntityToEntityId implements Converter<Object, EntityId> {

    private final AllEntities entities;
    private final EntityInfo info;

    @Inject
    public ConvertEntityToEntityId(AllEntities entities, EntityInfo info) {
        this.entities = entities;
        this.info = info;
    }

    public Object back(EntityId id) {
        if (id == null) {
            return null;
        }
        return entities.getEntityById(id);
    }

    public EntityId forth(Object entity) {
        if (entity == null) {
            return null;
        }
        // XXX: EntityInfo must be used instead of EntityManager, because the object
        // could be a transparent reference proxy, and EntityManager does not
        // know how to handle transparent references.
        return info.getEntityId(entity);
    }
}
