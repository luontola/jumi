// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.modules.options;

import com.google.inject.*;
import net.orfjackal.dimdwarf.gc.*;
import net.orfjackal.dimdwarf.gc.cms.ConcurrentMarkSweepCollector;

import java.math.BigInteger;

/**
 * @author Esko Luontola
 * @since 10.12.2008
 */
public class CmsGarbageCollectionOption extends AbstractModule {

    protected void configure() {
        bind(new TypeLiteral<GarbageCollector<BigInteger>>() {}).toProvider(GarbageCollectorProvider.class);
        bind(new TypeLiteral<MutatorListener<BigInteger>>() {}).toProvider(MutatorListenerProvider.class);
    }

    private static class GarbageCollectorProvider implements Provider<GarbageCollector<BigInteger>> {
        @Inject public Graph<BigInteger> graph;
        @Inject public NodeSetFactory factory;

        public GarbageCollector<BigInteger> get() {
            return new ConcurrentMarkSweepCollector<BigInteger>(graph, factory);
        }
    }

    private static class MutatorListenerProvider implements Provider<MutatorListener<BigInteger>> {
        @Inject public GarbageCollector<BigInteger> collector;

        public MutatorListener<BigInteger> get() {
            return collector.getMutatorListener();
        }
    }

    // TODO: reference counting collector
    // TODO: run the CMS collector periodically
}