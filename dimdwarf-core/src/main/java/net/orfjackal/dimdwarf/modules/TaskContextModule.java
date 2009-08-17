// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.modules;

import com.google.inject.*;
import net.orfjackal.dimdwarf.context.Context;
import net.orfjackal.dimdwarf.entities.EntityFlushingFilter;
import net.orfjackal.dimdwarf.scopes.*;
import net.orfjackal.dimdwarf.tasks.*;
import net.orfjackal.dimdwarf.tx.*;

import java.util.concurrent.Executor;

/**
 * @author Esko Luontola
 * @since 13.9.2008
 */
public class TaskContextModule extends AbstractModule {

    private static final int MAX_RETRIES = 5;

    protected void configure() {
        bindScope(TaskScoped.class, new TaskScope());
        bind(Context.class).to(TaskScopedContext.class);

        bind(TransactionCoordinator.class)
                .to(TransactionImpl.class)
                .in(TaskScoped.class);

        bind(RetryPolicy.class).toInstance(new RetryOnRetryableExceptionsANumberOfTimes(MAX_RETRIES));

        bind(Executor.class).annotatedWith(PlainTaskContext.class).to(TaskExecutor.class);
        bind(Executor.class).annotatedWith(RetryingTaskContext.class).to(RetryingTaskExecutor.class);
        bind(Executor.class).annotatedWith(SingleThreadFallbackTaskContext.class).to(SingleThreadFallbackTaskExecutor.class);
        bind(Executor.class).annotatedWith(TaskContext.class).to(Key.get(Executor.class, SingleThreadFallbackTaskContext.class));
    }

    @Provides
    Transaction transaction(TransactionCoordinator coordinator) {
        return coordinator.getTransaction();
    }

    @Provides
    Filter[] filters(TransactionFilter filter1, EntityFlushingFilter filter2) {
        return new Filter[]{filter1, filter2};
    }
}
