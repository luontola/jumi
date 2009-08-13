// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.gc.entities;

import net.orfjackal.dimdwarf.api.EntityId;
import net.orfjackal.dimdwarf.api.internal.EntityReference;
import net.orfjackal.dimdwarf.serial.*;

import javax.annotation.concurrent.Immutable;

/**
 * @author Esko Luontola
 * @since 11.12.2008
 */
@Immutable
public class EntityReferenceListener extends SerializationAdapter {

    // TODO: listen for EntityId instead of EntityReference

    @Override
    public void beforeSerialize(Object rootObject, Object obj, MetadataBuilder meta) {
        appendIfReference(obj, meta);
    }

    @Override
    public void afterDeserialize(Object obj, MetadataBuilder meta) {
        appendIfReference(obj, meta);
    }

    private static void appendIfReference(Object obj, MetadataBuilder meta) {
        if (obj instanceof EntityReference) {
            EntityReference<?> ref = (EntityReference<?>) obj;
            EntityId id = ref.getEntityId();
            meta.append(EntityReferenceListener.class, id);
        }
    }
}
