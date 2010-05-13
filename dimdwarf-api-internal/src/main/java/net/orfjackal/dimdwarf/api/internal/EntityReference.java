// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.api.internal;

import net.orfjackal.dimdwarf.api.EntityId;

/**
 * Reference to an entity, equivalent to Darkstar's ManagedReference.
 */
public interface EntityReference<T> {

    T get();

    EntityId getEntityId();
}
