// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.tasks;

import com.google.inject.*;
import net.orfjackal.dimdwarf.context.AbstractThreadContext;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class TaskContext extends AbstractThreadContext {

    @Inject
    public TaskContext(Injector injector) {
        super(injector);
    }
}
