// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.modules.options;

import com.google.inject.*;
import net.orfjackal.dimdwarf.api.EntityId;
import net.orfjackal.dimdwarf.gc.*;
import net.orfjackal.dimdwarf.tasks.util.IncrementalTask;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @author Esko Luontola
 * @since 11.12.2008
 */
public class NullGarbageCollectionOption extends AbstractModule {

    protected void configure() {
        bind(new TypeLiteral<GarbageCollector<EntityId>>() {}).toInstance(new NullGarbageCollector());
        bind(new TypeLiteral<MutatorListener<EntityId>>() {}).toInstance(new NullMutatorListener());
    }

    public static class NullGarbageCollector implements GarbageCollector<EntityId> {

        public List<? extends IncrementalTask> getCollectorStagesToExecute() {
            return Collections.emptyList();
        }

        public MutatorListener<EntityId> getMutatorListener() {
            return new NullMutatorListener();
        }

        public Enum<?> getColor(EntityId node) {
            return null;
        }
    }

    public static class NullMutatorListener implements MutatorListener<EntityId> {

        public void onNodeCreated(EntityId node) {
        }

        public void onReferenceCreated(@Nullable EntityId source, EntityId target) {
        }

        public void onReferenceRemoved(@Nullable EntityId source, EntityId target) {
        }
    }
}
