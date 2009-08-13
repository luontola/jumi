// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities.dao;

import com.google.inject.Inject;
import net.orfjackal.dimdwarf.api.EntityId;
import net.orfjackal.dimdwarf.db.*;

/**
 * @author Esko Luontola
 * @since 2.12.2008
 */
public class EntityDao
        extends DatabaseTableAdapter<EntityId, Blob, Blob, Blob>
        implements DatabaseTable<EntityId, Blob> {

    @Inject
    public EntityDao(@EntitiesTable DatabaseTable<Blob, Blob> parent,
                     ConvertEntityIdToBytes keys,
                     NoConversion<Blob> values) {
        super(parent, keys, values);
    }
}
