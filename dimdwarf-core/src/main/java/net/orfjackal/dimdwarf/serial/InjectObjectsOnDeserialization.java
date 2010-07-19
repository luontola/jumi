// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.serial;

import com.google.inject.*;

import javax.annotation.concurrent.Immutable;

@Immutable
public class InjectObjectsOnDeserialization {

    private final Injector injector;

    @Inject
    public InjectObjectsOnDeserialization(Injector injector) {
        this.injector = injector;
    }

    public void injectMembers(Object obj) {
        injector.injectMembers(obj);
    }
}
