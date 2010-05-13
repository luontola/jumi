// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.context;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;

@NotThreadSafe
public class FakeContext implements Context {

    private final Map<Class<?>, Object> services = new HashMap<Class<?>, Object>();

    public <T> FakeContext with(Class<T> type, T instance) {
        services.put(type, instance);
        return this;
    }

    public <T> T get(Class<T> service) {
        Object instance = services.get(service);
        if (instance == null) {
            throw new IllegalArgumentException("Not found: " + service);
        }
        return service.cast(instance);
    }
}
