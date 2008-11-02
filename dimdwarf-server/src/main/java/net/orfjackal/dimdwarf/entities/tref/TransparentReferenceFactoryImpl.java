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

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import net.orfjackal.dimdwarf.api.Entity;
import net.orfjackal.dimdwarf.api.ProxyType;
import net.orfjackal.dimdwarf.api.internal.EntityObject;
import net.orfjackal.dimdwarf.api.internal.EntityReference;
import net.orfjackal.dimdwarf.api.internal.TransparentReference;
import net.orfjackal.dimdwarf.entities.ReferenceFactory;
import net.orfjackal.dimdwarf.util.Cache;
import net.sf.cglib.proxy.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * The thread-safeness of this class depends on the injected dependencies.
 *
 * @author Esko Luontola
 * @since 26.1.2008
 */
@Singleton
public class TransparentReferenceFactoryImpl implements TransparentReferenceFactory {

    private final Cache<Class<?>, Factory> proxyFactories = new CglibProxyFactoryCache();
    private final Provider<ReferenceFactory> referenceFactory;

    @Inject
    public TransparentReferenceFactoryImpl(Provider<ReferenceFactory> referenceFactory) {
        this.referenceFactory = referenceFactory;
    }

    public TransparentReference createTransparentReference(EntityObject object) {
        Class<?> type = object.getClass();
        EntityReference<?> ref = referenceFactory.get().createReference(object);
        return newProxy(new TransparentReferenceImpl(type, ref));
    }

    public TransparentReference newProxy(TransparentReferenceImpl tref) {
        Factory factory = proxyFactories.get(tref.getType());
        return (TransparentReference) factory.newInstance(new Callback[]{
                new EntityCallback(tref),
                new TransparentReferenceCallback(tref)
        });
    }


    private static class CglibProxyFactoryCache extends Cache<Class<?>, Factory> {

        protected Factory newInstance(Class<?> type) {
            Enhancer e = new Enhancer();
            if (useConcreteSuperclass(type)) {
                e.setSuperclass(type);
            }
            e.setInterfaces(proxiedInterfaces(type));
            e.setCallbacks(new Callback[]{
                    new EntityCallback(null),
                    new TransparentReferenceCallback(null)
            });
            e.setCallbackFilter(new TransparentReferenceCallbackFilter());
            return (Factory) e.create();
        }

        private static boolean useConcreteSuperclass(Class<?> type) {
            Entity ann = type.getAnnotation(Entity.class);
            return ann != null && ann.value().equals(ProxyType.CLASS);
        }

        private static Class<?>[] proxiedInterfaces(Class<?> aClass) {
            List<Class<?>> results = new ArrayList<Class<?>>();
            for (Class<?> c = aClass; c != null; c = c.getSuperclass()) {
                for (Class<?> anInterface : c.getInterfaces()) {
                    assert !TransparentReference.class.equals(anInterface);
                    if (!EntityObject.class.isAssignableFrom(anInterface)) {
                        results.add(anInterface);
                    }
                }
            }
            results.add(TransparentReference.class);
            return results.toArray(new Class<?>[results.size()]);
        }
    }

    private static class TransparentReferenceCallbackFilter implements CallbackFilter {

        private static final int ENTITY_CALLBACK = 0;
        private static final int TRANSPARENT_REF_CALLBACK = 1;

        public int accept(Method method) {
            if (shouldDelegateToTransparentReference(method)) {
                return TRANSPARENT_REF_CALLBACK;
            } else {
                return ENTITY_CALLBACK;
            }
        }

        private static boolean shouldDelegateToTransparentReference(Method method) {
            return method.getDeclaringClass().equals(TransparentReference.class)
                    || (method.getDeclaringClass().equals(Object.class) && method.getName().equals("equals"))
                    || (method.getDeclaringClass().equals(Object.class) && method.getName().equals("hashCode"));
        }
    }

    private static class EntityCallback implements LazyLoader {

        private final TransparentReference tref;

        private EntityCallback(TransparentReference tref) {
            this.tref = tref;
        }

        public Object loadObject() throws Exception {
            return tref.getEntity();
        }
    }

    private static class TransparentReferenceCallback implements Dispatcher {

        private final TransparentReference tref;

        public TransparentReferenceCallback(TransparentReference tref) {
            this.tref = tref;
        }

        public Object loadObject() throws Exception {
            return tref;
        }
    }
}
