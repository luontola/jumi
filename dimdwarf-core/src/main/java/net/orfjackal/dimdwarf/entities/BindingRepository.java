// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import net.orfjackal.dimdwarf.db.DatabaseTable;

import javax.annotation.Nullable;

/**
 * @author Esko Luontola
 * @since 12.9.2008
 */
public interface BindingRepository extends DatabaseTable<String, Object> {

    @Nullable
    Object read(String binding);

    void update(String binding, Object entity);

    void delete(String binding);

    // TODO: introduce a public EntityBindings API similar to this class
}
