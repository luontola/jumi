// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.context;

import com.google.inject.*;

import javax.annotation.concurrent.Immutable;

/**
 * @author Esko Luontola
 * @since 13.9.2008
 */
@Immutable
public class ThreadScope implements Scope {

    private final Class<? extends AbstractThreadContext> contextType;

    public ThreadScope(Class<? extends AbstractThreadContext> contextType) {
        this.contextType = contextType;
    }

    public <T> Provider<T> scope(final Key<T> key, final Provider<T> unscoped) {
        return new Provider<T>() {
            public T get() {
                Context context = ThreadContext.getCurrentContext();
                checkInsideScope(context);
                return contextType.cast(context).scopedGet(key, unscoped);
            }
        };
    }

    private void checkInsideScope(Context context) {
        if (!contextType.isInstance(context)) {
            throw new IllegalStateException("Expected context " + contextType.getName() + " but was: " + context);
        }
    }
}
