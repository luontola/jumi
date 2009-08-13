// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import net.orfjackal.dimdwarf.api.EntityId;
import net.orfjackal.dimdwarf.db.DatabaseTable;

import javax.annotation.Nonnull;

/**
 * @author Esko Luontola
 * @since 31.8.2008
 */
public interface EntityRepository extends DatabaseTable<EntityId, Object> {

    @Nonnull
    Object read(EntityId id) throws EntityNotFoundException;

    void update(EntityId id, Object entity);

    void delete(EntityId id);
}
