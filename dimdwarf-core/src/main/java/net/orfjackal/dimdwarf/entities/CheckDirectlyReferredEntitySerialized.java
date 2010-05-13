// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import com.google.inject.Inject;
import net.orfjackal.dimdwarf.api.internal.EntityApi;
import net.orfjackal.dimdwarf.serial.*;

import javax.annotation.concurrent.Immutable;

@Immutable
public class CheckDirectlyReferredEntitySerialized extends SerializationAdapter {

    private final EntityApi entityApi;

    @Inject
    public CheckDirectlyReferredEntitySerialized(EntityApi entityApi) {
        this.entityApi = entityApi;
    }

    @Override
    public void beforeSerialize(Object rootObject, Object obj, MetadataBuilder meta) {
        if (obj != rootObject && entityApi.isEntity(obj)) {
            throw new IllegalArgumentException("Entity referred directly without an entity reference: " + obj.getClass());
        }
    }
}
