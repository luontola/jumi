// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import net.orfjackal.dimdwarf.api.internal.Entities;
import net.orfjackal.dimdwarf.serial.*;

import javax.annotation.concurrent.Immutable;

/**
 * @author Esko Luontola
 * @since 4.9.2008
 */
@Immutable
public class CheckDirectlyReferredEntitySerialized extends SerializationAdapter {

    @Override
    public void beforeSerialize(Object rootObject, Object obj, MetadataBuilder meta) {
        if (obj != rootObject && Entities.isEntity(obj)) {
            throw new IllegalArgumentException("Entity referred directly without an entity reference: " + obj.getClass());
        }
    }
}
