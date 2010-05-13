// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import net.orfjackal.dimdwarf.api.EntityId;
import net.orfjackal.dimdwarf.serial.*;

import javax.annotation.concurrent.Immutable;

@Immutable
public class EntityIdSerializationListener extends SerializationAdapter {

    @Override
    public void beforeSerialize(Object rootObject, Object obj, MetadataBuilder meta) {
        appendIfEntityId(obj, meta);
    }

    @Override
    public void afterDeserialize(Object obj, MetadataBuilder meta) {
        appendIfEntityId(obj, meta);
    }

    private static void appendIfEntityId(Object obj, MetadataBuilder meta) {
        if (obj instanceof EntityId) {
            EntityId id = (EntityId) obj;
            meta.append(EntityIdSerializationListener.class, id);
        }
    }
}
