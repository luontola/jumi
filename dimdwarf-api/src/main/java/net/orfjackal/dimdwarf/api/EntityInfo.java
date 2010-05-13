// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

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
