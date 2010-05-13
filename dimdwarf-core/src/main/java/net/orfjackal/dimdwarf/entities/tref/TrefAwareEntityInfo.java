// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities.tref;

import com.google.inject.Inject;
import net.orfjackal.dimdwarf.api.*;
import net.orfjackal.dimdwarf.api.internal.*;
import net.orfjackal.dimdwarf.entities.AllEntities;

import javax.annotation.concurrent.Immutable;

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
