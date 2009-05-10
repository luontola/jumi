// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.modules.options;

import com.google.inject.*;
import net.orfjackal.dimdwarf.gc.*;
import net.orfjackal.dimdwarf.tasks.util.IncrementalTask;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.*;

/**
 * @author Esko Luontola
 * @since 11.12.2008
 */
public class NullGarbageCollectionOption extends AbstractModule {

    protected void configure() {
        bind(new TypeLiteral<GarbageCollector<BigInteger>>() {}).toInstance(new NullGarbageCollector());
        bind(new TypeLiteral<MutatorListener<BigInteger>>() {}).toInstance(new NullMutatorListener());
    }

    public static class NullGarbageCollector implements GarbageCollector<BigInteger> {

        public List<? extends IncrementalTask> getCollectorStagesToExecute() {
            return Collections.emptyList();
        }

        public MutatorListener<BigInteger> getMutatorListener() {
            return new NullMutatorListener();
        }

        public Enum<?> getColor(BigInteger node) {
            return null;
        }
    }

    public static class NullMutatorListener implements MutatorListener<BigInteger> {

        public void onNodeCreated(BigInteger node) {
        }

        public void onReferenceCreated(@Nullable BigInteger source, BigInteger target) {
        }

        public void onReferenceRemoved(@Nullable BigInteger source, BigInteger target) {
        }
    }
}
