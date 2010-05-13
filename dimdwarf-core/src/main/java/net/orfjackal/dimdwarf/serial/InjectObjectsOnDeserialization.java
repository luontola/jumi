// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.serial;

import com.google.inject.*;

import javax.annotation.concurrent.Immutable;

@Immutable
public class InjectObjectsOnDeserialization extends SerializationAdapter {

    private final Injector injector;

    @Inject
    public InjectObjectsOnDeserialization(Injector injector) {
        this.injector = injector;
    }

    @Override
    public void afterResolve(Object obj, MetadataBuilder meta) {
        // TODO: Would afterDeserialize be better? May this accidentally inject transparent reference proxies?
        injector.injectMembers(obj);
    }
}
