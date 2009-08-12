// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities.tref;

import com.google.inject.Inject;
import net.orfjackal.dimdwarf.api.internal.*;
import net.orfjackal.dimdwarf.serial.*;

import javax.annotation.concurrent.Immutable;

/**
 * @author Esko Luontola
 * @since 5.9.2008
 */
@Immutable
public class ReplaceEntitiesWithTransparentReferences implements SerializationReplacer {

    private final TransparentReferenceFactory factory;
    private final EntityApi entityApi;

    @Inject
    public ReplaceEntitiesWithTransparentReferences(TransparentReferenceFactory factory, EntityApi entityApi) {
        this.factory = factory;
        this.entityApi = entityApi;
    }

    public Object replaceSerialized(Object rootObject, Object obj, MetadataBuilder meta) {
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

    public Object resolveDeserialized(Object obj, MetadataBuilder meta) {
        if (obj instanceof TransparentReferenceBackend) {
            return factory.newProxy((TransparentReferenceBackend) obj);
        }
        return obj;
    }
}
