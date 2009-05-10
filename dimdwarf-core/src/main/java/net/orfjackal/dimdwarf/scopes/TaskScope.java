// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.scopes;

import com.google.inject.*;
import net.orfjackal.dimdwarf.context.Context;
import net.orfjackal.dimdwarf.context.*;

import javax.annotation.concurrent.ThreadSafe;

/**
 * @author Esko Luontola
 * @since 13.9.2008
 */
@ThreadSafe
public class TaskScope implements Scope {

    public <T> Provider<T> scope(final Key<T> key, final Provider<T> unscoped) {
        return new Provider<T>() {
            public T get() {
                Context context = ThreadContext.getCurrentContext();
                if (!(context instanceof TaskScopedContext)) {
                    throw new IllegalStateException("Not inside task scope (context was " + context + ")");
                }
                return ((TaskScopedContext) context).scopedGet(key, unscoped);
            }
        };
    }
}
