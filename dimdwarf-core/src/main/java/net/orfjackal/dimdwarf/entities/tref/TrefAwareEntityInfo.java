// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities.tref;

import net.orfjackal.dimdwarf.api.*;
import net.orfjackal.dimdwarf.api.internal.*;
import net.orfjackal.dimdwarf.entities.AllEntities;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

@Immutable
public class TrefAwareEntityInfo implements EntityInfo {

    private final AllEntities entities;
    private final EntityApi entityApi;

    @Inject
    public TrefAwareEntityInfo(AllEntities entities, EntityApi entityApi) {
        this.entities = entities;
        this.entityApi = entityApi;
    }

    public EntityId getEntityId(Object entity) {
        if (entityApi.isEntity(entity)) {
            return getIdFromEntity(entity);
        }
        if (entityApi.isTransparentReference(entity)) {
            return getIdFromProxy(entity);
        }
        throw new IllegalArgumentException("Not an entity: " + entity);
    }

    private EntityId getIdFromEntity(Object entity) {
        return entities.getEntityId((EntityObject) entity);
    }

    private EntityId getIdFromProxy(Object proxy) {
        TransparentReference tref = (TransparentReference) proxy;
        return tref.getEntityReference$TREF().getEntityId();
    }
}
