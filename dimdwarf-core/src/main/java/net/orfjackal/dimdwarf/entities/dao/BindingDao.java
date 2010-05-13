// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities.dao;

import com.google.inject.Inject;
import net.orfjackal.dimdwarf.api.EntityId;
import net.orfjackal.dimdwarf.db.*;

public class BindingDao
        extends DatabaseTableAdapter<String, EntityId, Blob, Blob>
        implements DatabaseTable<String, EntityId> {

    @Inject
    public BindingDao(@BindingsTable DatabaseTable<Blob, Blob> parent,
                      ConvertStringToBytes keys,
                      ConvertEntityIdToBytes values) {
        super(parent, keys, values);
    }
}
