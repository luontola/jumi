// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import com.google.inject.Inject;
import net.orfjackal.dimdwarf.tasks.Filter;

import javax.annotation.concurrent.Immutable;

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
