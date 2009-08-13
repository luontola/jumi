// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import net.orfjackal.dimdwarf.api.EntityId;
import net.orfjackal.dimdwarf.serial.*;

import javax.annotation.concurrent.Immutable;

/**
 * @author Esko Luontola
 * @since 11.12.2008
 */
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
