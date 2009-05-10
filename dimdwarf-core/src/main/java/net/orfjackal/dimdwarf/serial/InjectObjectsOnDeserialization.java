// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.serial;

import com.google.inject.*;

import javax.annotation.concurrent.Immutable;

/**
 * @author Esko Luontola
 * @since 14.9.2008
 */
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
