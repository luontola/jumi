// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

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
