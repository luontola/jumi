// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.modules.options;

import com.google.inject.*;
import net.orfjackal.dimdwarf.api.EntityId;
import net.orfjackal.dimdwarf.gc.*;
import net.orfjackal.dimdwarf.gc.cms.ConcurrentMarkSweepCollector;

/**
 * @author Esko Luontola
 * @since 10.12.2008
 */
public class CmsGarbageCollectionOption extends AbstractModule {

    protected void configure() {
        bind(new TypeLiteral<GarbageCollector<EntityId>>() {}).toProvider(GarbageCollectorProvider.class);
        bind(new TypeLiteral<MutatorListener<EntityId>>() {}).toProvider(MutatorListenerProvider.class);
    }

    private static class GarbageCollectorProvider implements Provider<GarbageCollector<EntityId>> {
        @Inject public Graph<EntityId> graph;
        @Inject public NodeSetFactory factory;

        public GarbageCollector<EntityId> get() {
            return new ConcurrentMarkSweepCollector<EntityId>(graph, factory);
        }
    }

    private static class MutatorListenerProvider implements Provider<MutatorListener<EntityId>> {
        @Inject public GarbageCollector<EntityId> collector;

        public MutatorListener<EntityId> get() {
            return collector.getMutatorListener();
        }
    }

    // TODO: reference counting collector
    // TODO: run the CMS collector periodically
}