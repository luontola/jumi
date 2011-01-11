// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities.tref;

import com.google.inject.Injector;
import net.orfjackal.dimdwarf.entities.*;

import javax.inject.Inject;

public class TrefAwareEntitySerializationFilter implements EntitySerializationFilter {

    private final TransparentReferenceSerializationSupport trefSupport;
    private final SerializationAllowedPolicy policy;
    private final Injector injector;

    @Inject
    public TrefAwareEntitySerializationFilter(TransparentReferenceSerializationSupport trefSupport,
                                              SerializationAllowedPolicy policy,
                                              Injector injector) {
        this.trefSupport = trefSupport;
        this.policy = policy;
        this.injector = injector;
    }

    public Object replaceSerialized(Object rootObject, Object obj) {
        obj = trefSupport.replaceDirectlyReferredEntityWithTransparentReference(rootObject, obj);
        policy.checkSerializationAllowed(rootObject, obj);
        return obj;
    }

    public Object resolveDeserialized(Object obj) {
        obj = trefSupport.initDeserializedTransparentReference(obj);
        // TODO: Would before tref handling be better? May this accidentally inject transparent reference proxies?
        // TODO: Would using com.google.inject.MembersInjector make sense?
        // TODO: Inject only objects with a marker interface? Need to profile the current performance first.
        injector.injectMembers(obj);
        return obj;
    }
}
