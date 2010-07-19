// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities.tref;

import com.google.inject.Inject;
import net.orfjackal.dimdwarf.entities.*;
import net.orfjackal.dimdwarf.serial.InjectObjectsOnDeserialization;

public class TrefAwareEntitySerializationFilter implements EntitySerializationFilter {

    private final ReplaceEntitiesWithTransparentReferences replaceEntitiesWithTransparentReferences;
    private final CheckDirectlyReferredEntitySerialized checkDirectlyReferredEntitySerialized;
    private final CheckInnerClassSerialized checkInnerClassSerialized;
    private final InjectObjectsOnDeserialization injectObjectsOnDeserialization;

    @Inject
    public TrefAwareEntitySerializationFilter(ReplaceEntitiesWithTransparentReferences replaceEntitiesWithTransparentReferences,
                                                CheckDirectlyReferredEntitySerialized checkDirectlyReferredEntitySerialized,
                                                CheckInnerClassSerialized checkInnerClassSerialized,
                                                InjectObjectsOnDeserialization injectObjectsOnDeserialization) {
        this.replaceEntitiesWithTransparentReferences = replaceEntitiesWithTransparentReferences;
        this.checkDirectlyReferredEntitySerialized = checkDirectlyReferredEntitySerialized;
        this.checkInnerClassSerialized = checkInnerClassSerialized;
        this.injectObjectsOnDeserialization = injectObjectsOnDeserialization;
    }

    public Object replaceSerialized(Object rootObject, Object obj) {
        obj = replaceEntitiesWithTransparentReferences.replaceSerialized(rootObject, obj);
        checkDirectlyReferredEntitySerialized.checkBeforeSerialized(rootObject, obj);
        checkInnerClassSerialized.checkBeforeSerialized(obj);
        return obj;
    }

    public Object resolveDeserialized(Object obj) {
        obj = replaceEntitiesWithTransparentReferences.resolveDeserialized(obj);
        injectObjectsOnDeserialization.injectMembers(obj); // TODO: Would before tref replacement be better? May this accidentally inject transparent reference proxies?
        return obj;
    }
}
