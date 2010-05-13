// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import com.google.inject.Inject;
import net.orfjackal.dimdwarf.api.EntityId;
import net.orfjackal.dimdwarf.api.internal.*;

import javax.annotation.concurrent.Immutable;

/**
 * @author Esko Luontola
 * @since 3.11.2008
 */
@Immutable
public class EntityReferenceFactoryImpl implements EntityReferenceFactory {

    private final AllEntities entities;

    @Inject
    public EntityReferenceFactoryImpl(AllEntities entities) {
        this.entities = entities;
    }

    public <T> EntityReference<T> createReference(T entity) {
        EntityId id = entities.getEntityId((EntityObject) entity);
        return new EntityReferenceImpl<T>(id, entity);
    }
}
