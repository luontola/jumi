// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.api;

import fi.jumi.core.events.suiteListener.*;

import javax.annotation.concurrent.NotThreadSafe;
import java.lang.reflect.*;
import java.util.*;

/**
 * @deprecated Replaced by {@link fi.jumi.core.util.MethodInvocationSpy}
 */
@Deprecated
@NotThreadSafe
public class SuiteListenerSpy extends SuiteListenerToEvent {

    public final Map<Method, Object[]> methodInvocations;

    public SuiteListenerSpy() {
        this(new HashMap<>());
    }

    private SuiteListenerSpy(Map<Method, Object[]> methodInvocations) {
        super(new EventToSuiteListener(
                (SuiteListener) Proxy.newProxyInstance(
                        SuiteListener.class.getClassLoader(),
                        new Class[]{SuiteListener.class},
                        (proxy, method, args) -> {
                            methodInvocations.put(method, args);
                            return null;
                        })));
        this.methodInvocations = methodInvocations;
    }
}
