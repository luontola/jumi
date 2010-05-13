// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import net.orfjackal.dimdwarf.api.EntityId;
import net.orfjackal.dimdwarf.api.internal.EntityObject;
import net.orfjackal.dimdwarf.db.IterableKeys;

/**
 * @author Esko Luontola
 * @since 31.8.2008
 */
public interface AllEntities extends IterableKeys<EntityId> {

    EntityId getEntityId(EntityObject entity);

    EntityObject getEntityById(EntityId id);
}
