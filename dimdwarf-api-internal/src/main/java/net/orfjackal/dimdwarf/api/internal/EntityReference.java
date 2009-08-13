// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.api.internal;

import net.orfjackal.dimdwarf.api.EntityId;

/**
 * Reference to an entity, equivalent to Darkstar's ManagedReference.
 *
 * @author Esko Luontola
 * @since 15.8.2008
 */
public interface EntityReference<T> {

    T get();

    EntityId getEntityId();
}
