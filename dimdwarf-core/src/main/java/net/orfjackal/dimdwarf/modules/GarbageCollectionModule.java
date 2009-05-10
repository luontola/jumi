// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.modules;

import com.google.inject.*;
import net.orfjackal.dimdwarf.entities.dao.EntityDao;
import net.orfjackal.dimdwarf.gc.*;
import net.orfjackal.dimdwarf.gc.entities.*;
import net.orfjackal.dimdwarf.util.Objects;

import java.math.BigInteger;

/**
 * @author Esko Luontola
 * @since 10.12.2008
 */
public class GarbageCollectionModule extends AbstractModule {

    protected void configure() {
        bind(new TypeLiteral<Graph<BigInteger>>() {}).to(EntityGraphWrapper.class);
        bind(NodeSetFactory.class).to(EntityNodeSetFactory.class);

        bind(GarbageCollectorManager.class).to(GarbageCollectorManagerImpl.class);
    }

    public static class EntityNodeSetFactory implements NodeSetFactory {
        @Inject public EntityDao entities;

        public <T> NodeSet<T> create(String name) {
            return Objects.uncheckedCast(new EntityNodeSet(name, entities));
        }
    }
}
