// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.util;

import fi.jumi.actors.eventizers.Eventizer;

import java.lang.reflect.*;
import java.util.*;

public class MethodInvocationSpy<T> {

    public final Map<Method, Object[]> methodInvocations = new HashMap<>();
    private final Eventizer<T> eventizer;

    public MethodInvocationSpy(Eventizer<T> eventizer) {
        this.eventizer = eventizer;
    }

    public T getProxy() {
        return eventizer.getType().cast(
                Proxy.newProxyInstance(
                        eventizer.getType().getClassLoader(),
                        new Class[]{eventizer.getType()},
                        (proxy, method, args) -> {
                            methodInvocations.put(method, args);
                            return null;
                        }));
    }
}
