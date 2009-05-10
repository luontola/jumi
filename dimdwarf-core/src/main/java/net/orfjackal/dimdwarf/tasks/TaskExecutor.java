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
