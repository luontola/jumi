// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.context;

import com.google.inject.*;

import javax.annotation.concurrent.Immutable;

@Immutable
public class ThreadScope implements Scope {

    private final Class<? extends BaseContext> contextType;

    public ThreadScope(Class<? extends BaseContext> contextType) {
        this.contextType = contextType;
    }

    public <T> Provider<T> scope(final Key<T> key, final Provider<T> unscoped) {
        return new Provider<T>() {
            public T get() {
                Context context = ThreadContext.getCurrentContext();
                checkInsideScope(context);
                return contextType.cast(context).scopedGet(key, unscoped);
            }

            @Override
            public String toString() {
                return unscoped.toString();
            }
        };
    }

    private void checkInsideScope(Context context) {
        if (!contextType.isInstance(context)) {
            throw new IllegalStateException("Expected context " + contextType.getName() + " but was: " + context);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + contextType.getSimpleName() + ")";
    }
}
