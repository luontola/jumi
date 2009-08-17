// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.scopes;

import com.google.inject.*;
import net.orfjackal.dimdwarf.context.Context;
import net.orfjackal.dimdwarf.context.*;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;

/**
 * When {@code TaskScopedContext} is installed as the current {@link ThreadContext},
 * then that thread is in a task scope identified by the {@code TaskScopedContext} instance.
 *
 * @author Esko Luontola
 * @since 13.9.2008
 */
@NotThreadSafe
public class TaskScopedContext implements Context {

    // TODO: when CoordinatorScope is created, extract AbstractThreadContext from this class

    private final Map<Key<?>, Object> cache = new HashMap<Key<?>, Object>();
    private final Injector injector;

    @Inject
    public TaskScopedContext(Injector injector) {
        this.injector = injector;
    }

    public <T> T get(Class<T> service) {
        return injector.getInstance(service);
    }

    <T> T scopedGet(Key<T> key, Provider<T> unscoped) {
        T value = (T) cache.get(key);
        if (value == null) {
            value = unscoped.get();
            cache.put(key, value);
        }
        return value;
    }
}
