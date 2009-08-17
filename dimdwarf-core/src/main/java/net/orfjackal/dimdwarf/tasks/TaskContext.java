// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.tasks;

import com.google.inject.*;
import net.orfjackal.dimdwarf.context.AbstractThreadContext;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * @author Esko Luontola
 * @since 13.9.2008
 */
@NotThreadSafe
public class TaskContext extends AbstractThreadContext {

    @Inject
    public TaskContext(Injector injector) {
        super(injector);
    }
}
