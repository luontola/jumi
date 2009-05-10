// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.context;

import java.util.*;

/**
 * This class is NOT thread-safe.
 *
 * @author Esko Luontola
 * @since 5.9.2008
 */
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
