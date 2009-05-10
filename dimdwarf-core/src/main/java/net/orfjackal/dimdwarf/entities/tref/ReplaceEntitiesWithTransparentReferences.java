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

    @Inject
    public ReplaceEntitiesWithTransparentReferences(TransparentReferenceFactory factory) {
        this.factory = factory;
    }

    public Object replaceSerialized(Object rootObject, Object obj, MetadataBuilder meta) {
        if (obj != rootObject && Entities.isEntity(obj)) {
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
        if (obj instanceof TransparentReferenceImpl) {
            return factory.newProxy((TransparentReferenceImpl) obj);
        }
        return obj;
    }
}
