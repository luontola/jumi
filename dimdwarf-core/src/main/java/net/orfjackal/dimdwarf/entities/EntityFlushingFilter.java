// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import com.google.inject.Inject;
import net.orfjackal.dimdwarf.tasks.Filter;

import javax.annotation.concurrent.Immutable;

/**
 * @author Esko Luontola
 * @since 12.11.2008
 */
@Immutable
public class EntityFlushingFilter implements Filter {

    private final EntityManagerImpl entityManager;

    @Inject
    public EntityFlushingFilter(EntityManagerImpl entityManager) {
        this.entityManager = entityManager;
    }

    public void filter(Runnable nextInChain) {
        nextInChain.run();
        entityManager.flushAllEntitiesToDatabase();
    }
}
