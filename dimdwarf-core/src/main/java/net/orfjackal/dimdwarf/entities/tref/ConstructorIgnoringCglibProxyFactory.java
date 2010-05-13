// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities.tref;

import net.sf.cglib.proxy.Callback;
import org.objenesis.Objenesis;
import org.objenesis.instantiator.ObjectInstantiator;

import java.lang.reflect.Method;

public class ConstructorIgnoringCglibProxyFactory extends NullCglibProxyFactory {

    private final ObjectInstantiator instantiator;
    private final Method setThreadCallbacks;
    private final Method bindCallbacks;

    public ConstructorIgnoringCglibProxyFactory(Class<?> proxyClass, Objenesis objenesis) {
        instantiator = objenesis.getInstantiatorOf(proxyClass);
        try {
            // XXX: It would be best to integrate the behaviour of bypassing constructors to CGLIB, instead of invoking CGLIB's internals here.
            setThreadCallbacks = proxyClass.getDeclaredMethod("CGLIB$SET_THREAD_CALLBACKS", Callback[].class);
            bindCallbacks = proxyClass.getDeclaredMethod("CGLIB$BIND_CALLBACKS", Object.class);
            bindCallbacks.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Unable to create factory for " + proxyClass, e);
        }
    }

    public Object newInstance(Callback[] callbacks) {
        try {
            return newProxyWithoutCallingConstructor(callbacks);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object newProxyWithoutCallingConstructor(Callback[] callbacks) throws Exception {
        // TODO: generate bytecode for calling these methods, or is reflection fast enough?
        setThreadCallbacks.invoke(null, (Object) callbacks);
        Object obj = instantiator.newInstance();
        bindCallbacks.invoke(null, obj);
        setThreadCallbacks.invoke(null, (Object) null);
        return obj;
    }
}
