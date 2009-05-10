// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import com.google.inject.Inject;
import net.orfjackal.dimdwarf.api.internal.*;

import javax.annotation.concurrent.Immutable;
import java.math.BigInteger;

/**
 * @author Esko Luontola
 * @since 3.11.2008
 */
@Immutable
public class ReferenceFactoryImpl implements ReferenceFactory {

    private final EntityManager entityManager;

    @Inject
    public ReferenceFactoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public <T> EntityReference<T> createReference(T entity) {
        BigInteger id = entityManager.getEntityId((EntityObject) entity);
        return new EntityReferenceImpl<T>(id, entity);
    }
}
