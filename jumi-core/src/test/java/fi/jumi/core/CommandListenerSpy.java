// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core;

import fi.jumi.core.events.commandListener.*;

import javax.annotation.concurrent.NotThreadSafe;
import java.lang.reflect.*;
import java.util.*;

@NotThreadSafe
public class CommandListenerSpy extends CommandListenerToEvent {

    public final Map<Method, Object[]> methodInvocations;

    public CommandListenerSpy() {
        this(new HashMap<>());
    }

    private CommandListenerSpy(Map<Method, Object[]> methodInvocations) {
        super(new EventToCommandListener(
                (CommandListener) Proxy.newProxyInstance(
                        CommandListener.class.getClassLoader(),
                        new Class[]{CommandListener.class},
                        (proxy, method, args) -> {
                            methodInvocations.put(method, args);
                            return null;
                        })));
        this.methodInvocations = methodInvocations;
    }
}
