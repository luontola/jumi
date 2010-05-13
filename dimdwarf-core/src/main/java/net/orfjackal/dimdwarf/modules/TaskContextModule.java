// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.modules;

import com.google.inject.*;
import net.orfjackal.dimdwarf.context.*;
import net.orfjackal.dimdwarf.entities.EntityFlushingFilter;
import net.orfjackal.dimdwarf.tasks.*;
import net.orfjackal.dimdwarf.tx.*;

import java.util.concurrent.Executor;

public class TaskContextModule extends AbstractModule {

    private static final int MAX_RETRIES = 5;

    protected void configure() {
        bindScope(TaskScoped.class, new ThreadScope(TaskContext.class));
        bind(Context.class).annotatedWith(Task.class).to(TaskContext.class);

        bind(TransactionCoordinator.class)
                .to(TransactionContext.class)
                .in(TaskScoped.class);

        bind(RetryPolicy.class).toInstance(new RetryOnRetryableExceptionsANumberOfTimes(MAX_RETRIES));

        bind(Executor.class).annotatedWith(PlainTaskContext.class).to(TaskExecutor.class);
        bind(Executor.class).annotatedWith(RetryingTaskContext.class).to(RetryingTaskExecutor.class);
        bind(Executor.class).annotatedWith(SingleThreadFallbackTaskContext.class).to(SingleThreadFallbackTaskExecutor.class);
        bind(Executor.class).annotatedWith(Task.class).to(Key.get(Executor.class, SingleThreadFallbackTaskContext.class));
    }

    @Provides
    Transaction transaction(TransactionCoordinator coordinator) {
        return coordinator.getTransaction();
    }

    @Provides
    @Task
    FilterChain filters(TransactionFilter filter1, EntityFlushingFilter filter2) {
        return new FilterChain(new Filter[]{
                filter1, filter2
        });
    }
}
