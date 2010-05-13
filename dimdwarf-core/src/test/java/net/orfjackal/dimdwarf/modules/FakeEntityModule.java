// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.modules;

import com.google.inject.AbstractModule;
import jdave.Specification;
import net.orfjackal.dimdwarf.entities.*;

public class FakeEntityModule extends AbstractModule {

    private final Specification<?> spec;

    public FakeEntityModule(Specification<?> spec) {
        this.spec = spec;
    }

    protected void configure() {
        EntitiesLoadedInMemory entities = spec.dummy(EntitiesLoadedInMemory.class);

        // TaskContextModule has a transitive dependency to these classes:
        bind(EntityFlushingFilter.class)
                .toInstance(new EntityFlushingFilter(entities));
    }
}
