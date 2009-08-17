// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.tasks;

import com.google.inject.*;
import net.orfjackal.dimdwarf.context.Context;
import net.orfjackal.dimdwarf.context.*;

import javax.annotation.concurrent.Immutable;
import java.util.concurrent.Executor;

/**
 * @author Esko Luontola
 * @since 13.9.2008
 */
@Immutable
public class TaskExecutor implements Executor {

    // TODO: when CoordinatorScope is created, convert this class to ThreadContextExecutor
    // We will probably need to annotate the context in new architecture, as CoordinatorContext/Scope will be implemented.
    // It might be possible to generalize TaskExecutor into ThreadContextExecutor and use custom providers for Task and Coordinator scopes.

    private final Provider<Context> contextProvider;
    private final Provider<FilterChain> filterProvider;

    @Inject
    public TaskExecutor(Provider<Context> contextProvider,
                        Provider<FilterChain> filterProvider) {
        this.contextProvider = contextProvider;
        this.filterProvider = filterProvider;
    }

    public void execute(final Runnable command) {
        ThreadContext.runInContext(contextProvider.get(), new Runnable() {
            public void run() {
                filterProvider.get().execute(command);
            }
        });
    }
}
