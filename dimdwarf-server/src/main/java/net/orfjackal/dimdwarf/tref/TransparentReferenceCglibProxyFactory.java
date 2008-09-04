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

package net.orfjackal.dimdwarf.tref;

import net.orfjackal.dimdwarf.api.Entity;
import net.orfjackal.dimdwarf.api.internal.EntityReference;
import net.orfjackal.dimdwarf.util.Cache;
import net.sf.cglib.proxy.*;

import java.lang.reflect.Method;

/**
 * @author Esko Luontola
 * @since 26.1.2008
 */
public class TransparentReferenceCglibProxyFactory implements TransparentReferenceFactory {

    private final Cache<Class<?>, Factory> cache = new CglibProxyFactoryCache();

    public TransparentReference createTransparentReference(Entity object) {
        Class<?> type = object.getClass();
        EntityReference<?> reference = AppContext.getDataManager().createReference(object);
        return newProxy(new TransparentReferenceImpl(type, reference));
    }

    public TransparentReference newProxy(TransparentReferenceImpl ref) {
        Factory factory = cache.get(ref.getType());
        return (TransparentReference) factory.newInstance(new Callback[]{
                new ManagedObjectCallback(ref),
                new TransparentReferenceCallback(ref)
        });
    }

    private static class CglibProxyFactoryCache extends Cache<Class<?>, Factory> {

        protected Factory newInstance(Class<?> type) {
            Enhancer e = new Enhancer();
            e.setInterfaces(TransparentReferenceUtil.proxiedInterfaces(type));
            e.setCallbacks(new Callback[]{
                    new ManagedObjectCallback(null),
                    new TransparentReferenceCallback(null)
            });
            e.setCallbackFilter(new TransparentReferenceCallbackFilter());
            return (Factory) e.create();
        }
    }

    private static class ManagedObjectCallback implements LazyLoader {

        private final TransparentReference ref;

        private ManagedObjectCallback(TransparentReference ref) {
            this.ref = ref;
        }

        public Object loadObject() throws Exception {
            return ref.getManagedObject();
        }
    }

    private static class TransparentReferenceCallback implements Dispatcher {

        private final TransparentReference ref;

        public TransparentReferenceCallback(TransparentReference ref) {
            this.ref = ref;
        }

        public Object loadObject() throws Exception {
            return ref;
        }
    }

    private static class TransparentReferenceCallbackFilter implements CallbackFilter {

        private static final int MANAGED_OBJECT = 0;
        private static final int TRANSPARENT_REFERENCE = 1;

        public int accept(Method method) {
            if (TransparentReferenceUtil.delegateToTransparentReference(method)) {
                return TRANSPARENT_REFERENCE;
            } else {
                return MANAGED_OBJECT;
            }
        }
    }
}
