/*
 * This file is part of Dimdwarf Application Server <http://dimdwarf.sourceforge.net/>
 *
 * Copyright (c) 2008, Esko Luontola. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *
 *     * Neither the name of the copyright holder nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.orfjackal.dimdwarf.entities.tref;

import net.sf.cglib.proxy.Callback;
import org.objenesis.Objenesis;
import org.objenesis.instantiator.ObjectInstantiator;

import java.lang.reflect.Method;

/**
 * @author Esko Luontola
 * @since 27.12.2008
 */
public class ConstructorIgnoringCglibProxyFactory extends NullCglibProxyFactory {

    private final ObjectInstantiator instantiator;
    private final Method setThreadCallbacks;
    private final Method bindCallbacks;

    public ConstructorIgnoringCglibProxyFactory(Class<?> proxyClass, Objenesis objenesis) {
        instantiator = objenesis.getInstantiatorOf(proxyClass);
        try {
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
