// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

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
