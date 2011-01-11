// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import net.orfjackal.dimdwarf.tasks.Filter;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

@Immutable
public class EntityFlushingFilter implements Filter {

    private final EntitiesLoadedInMemory entities;

    @Inject
    public EntityFlushingFilter(EntitiesLoadedInMemory entities) {
        this.entities = entities;
    }

    public void filter(Runnable nextInChain) {
        nextInChain.run();
        entities.flushToDatabase();
    }
}
