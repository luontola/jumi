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
        bind(Transaction.class).toProvider(TransactionProvider.class);
        bind(Filter[].class).toProvider(FilterListProvider.class);

        bind(RetryPolicy.class).toInstance(new RetryOnRetryableExceptionsANumberOfTimes(MAX_RETRIES));

        bind(Executor.class).annotatedWith(PlainTaskContext.class).to(TaskExecutor.class);
        bind(Executor.class).annotatedWith(RetryingTaskContext.class).to(RetryingTaskExecutor.class);
        bind(Executor.class).annotatedWith(SingleThreadFallbackTaskContext.class).to(SingleThreadFallbackTaskExecutor.class);
        bind(Executor.class).annotatedWith(TaskContext.class).to(Key.get(Executor.class, SingleThreadFallbackTaskContext.class));
    }

    private static class TransactionProvider implements Provider<Transaction> {
        @Inject public TransactionCoordinator coordinator;

        public Transaction get() {
            return coordinator.getTransaction();
        }
    }

    private static class FilterListProvider implements Provider<Filter[]> {
        @Inject public TransactionFilter filter1;
        @Inject public EntityFlushingFilter filter2;

        public Filter[] get() {
            return new Filter[]{filter1, filter2};
        }
    }
}
