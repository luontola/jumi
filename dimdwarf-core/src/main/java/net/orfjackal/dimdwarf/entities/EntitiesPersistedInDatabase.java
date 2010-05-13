// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import net.orfjackal.dimdwarf.api.EntityId;

import javax.annotation.Nonnull;

public interface EntitiesPersistedInDatabase {

    @Nonnull
    Object read(EntityId id) throws EntityNotFoundException;

    void update(EntityId id, Object entity);
}
