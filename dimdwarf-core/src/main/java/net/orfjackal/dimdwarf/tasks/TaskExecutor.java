// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.tasks;

import com.google.inject.*;
import net.orfjackal.dimdwarf.context.*;

import javax.annotation.concurrent.Immutable;
import java.util.concurrent.Executor;

@Immutable
public class TaskExecutor implements Executor {

    private final Provider<Context> context;
    private final Provider<FilterChain> filters;

    @Inject
    public TaskExecutor(@Task Provider<Context> context,
                        @Task Provider<FilterChain> filters) {
        this.context = context;
        this.filters = filters;
    }

    public void execute(final Runnable command) {
        ThreadContext.runInContext(context.get(), new Runnable() {
            public void run() {
                filters.get().execute(command);
            }
        });
    }
}
