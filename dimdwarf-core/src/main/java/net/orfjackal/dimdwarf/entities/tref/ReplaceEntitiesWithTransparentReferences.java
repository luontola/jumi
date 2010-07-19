// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities.tref;

import com.google.inject.Inject;
import net.orfjackal.dimdwarf.api.internal.*;
import net.orfjackal.dimdwarf.serial.SerializationFilter;

import javax.annotation.concurrent.Immutable;

@Immutable
public class ReplaceEntitiesWithTransparentReferences implements SerializationFilter {

    private final TransparentReferenceFactory factory;
    private final EntityApi entityApi;

    @Inject
    public ReplaceEntitiesWithTransparentReferences(TransparentReferenceFactory factory, EntityApi entityApi) {
        this.factory = factory;
        this.entityApi = entityApi;
    }

    public Object replaceSerialized(Object rootObject, Object obj) {
        if (obj != rootObject && entityApi.isEntity(obj)) {
            return createTransparentReferenceForSerialization(obj);
        }
        return obj;
    }

    private Object createTransparentReferenceForSerialization(Object entity) {
        TransparentReference notSerializableProxy = factory.createTransparentReference(entity);
        // The call to writeReplace() is needed because ObjectOutputStream#replaceObject does not check
        // whether the returned objects have a writeReplace() method.
        return notSerializableProxy.writeReplace();
    }

    public Object resolveDeserialized(Object obj) {
        if (obj instanceof TransparentReferenceBackend) {
            return factory.newProxy((TransparentReferenceBackend) obj);
        }
        return obj;
    }
}
