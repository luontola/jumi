// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import net.orfjackal.dimdwarf.api.*;
import net.orfjackal.dimdwarf.db.Converter;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

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
