// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.modules;

import com.google.inject.*;
import net.orfjackal.dimdwarf.api.*;
import net.orfjackal.dimdwarf.entities.BindingRepositoryImpl;
import net.orfjackal.dimdwarf.scheduler.*;
import net.orfjackal.dimdwarf.util.*;

import java.util.concurrent.*;

/**
 * @author Esko Luontola
 * @since 27.11.2008
 */
public class TaskSchedulingModule extends AbstractModule {

    protected void configure() {
        bind(TaskScheduler.class).to(TaskSchedulerImpl.class);
        bind(TaskProducer.class).to(TaskSchedulerImpl.class);

        bind(RecoverableSetFactory.class).to(RecoverableSetFactoryImpl.class);
        bind(Clock.class).to(SystemClock.class);
    }

    @Provides
    ExecutorService executorService() {
        return Executors.newCachedThreadPool();
    }

    private static class RecoverableSetFactoryImpl implements RecoverableSetFactory {
        @Inject public Provider<BindingRepositoryImpl> bindings;
        @Inject public Provider<EntityInfo> info;

        public <T> RecoverableSet<T> create(String prefix) {
            return new RecoverableSetImpl<T>(prefix, bindings, info);
        }
    }
}
