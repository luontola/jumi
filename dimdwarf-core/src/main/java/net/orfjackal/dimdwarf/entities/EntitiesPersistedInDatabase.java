// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import net.orfjackal.dimdwarf.api.EntityId;

import javax.annotation.Nonnull;

public interface EntitiesPersistedInDatabase {

    @Nonnull
    Object read(EntityId id) throws EntityNotFoundException;

    void update(EntityId id, Object entity);
}
