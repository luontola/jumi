// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.context;

import com.google.inject.*;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;

@NotThreadSafe
public abstract class AbstractThreadContext implements Context {

    private final Map<Key<?>, Object> cache = new HashMap<Key<?>, Object>();
    private final Injector injector;

    public AbstractThreadContext(Injector injector) {
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
