// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.api;

public interface EntityInfo {

    /**
     * Returns a unique ID for the specified entity. The parameter may be an entity
     * or a transparent reference proxy of an entity.
     *
     * @throws IllegalArgumentException if the object is not an entity.
     */
    EntityId getEntityId(Object entity);
}
