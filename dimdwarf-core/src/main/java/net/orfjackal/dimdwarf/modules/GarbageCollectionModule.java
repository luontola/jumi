/*
 * This file is part of Dimdwarf Application Server <http://dimdwarf.sourceforge.net/>
 *
 * Copyright (c) 2008, Esko Luontola. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *
 *     * Neither the name of the copyright holder nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.orfjackal.dimdwarf.modules;

import com.google.inject.*;
import net.orfjackal.dimdwarf.gc.*;
import net.orfjackal.dimdwarf.gc.cms.ConcurrentMarkSweepCollector;
import net.orfjackal.dimdwarf.gc.entities.*;

import javax.annotation.Nullable;
import java.math.BigInteger;

/**
 * @author Esko Luontola
 * @since 10.12.2008
 */
public class GarbageCollectionModule extends AbstractModule {

    protected void configure() {
        bind(new TypeLiteral<Graph<BigInteger>>() {}).to(EntityGraphWrapper.class);
        bind(new TypeLiteral<GarbageCollector<BigInteger>>() {}).toProvider(GarbageCollectorProvider.class);
        bind(new TypeLiteral<MutatorListener<BigInteger>>() {}).toInstance(new MutatorListener<BigInteger>() {
            // TODO: use a real listener
            public void onReferenceCreated(@Nullable BigInteger source, BigInteger target) {
            }

            public void onReferenceRemoved(@Nullable BigInteger source, BigInteger target) {
            }
        });
        bind(GarbageCollectorManager.class).to(GarbageCollectorManagerImpl.class);
    }

    private static class GarbageCollectorProvider implements Provider<GarbageCollector<BigInteger>> {
        @Inject public Graph<BigInteger> graph;

        public GarbageCollector<BigInteger> get() {
            return new ConcurrentMarkSweepCollector<BigInteger>(graph);
        }
    }

    // TODO: reference counting collector
    // TODO: run the CMS collector periodically
}
