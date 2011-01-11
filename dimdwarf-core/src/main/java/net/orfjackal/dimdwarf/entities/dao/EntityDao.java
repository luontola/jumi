// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities.dao;

import net.orfjackal.dimdwarf.api.EntityId;
import net.orfjackal.dimdwarf.db.*;

import javax.inject.Inject;

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
