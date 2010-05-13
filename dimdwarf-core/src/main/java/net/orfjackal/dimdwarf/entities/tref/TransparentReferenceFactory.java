// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities.tref;

import com.google.inject.*;
import net.orfjackal.dimdwarf.api.*;
import net.orfjackal.dimdwarf.api.internal.*;
import net.orfjackal.dimdwarf.entities.EntityReferenceFactory;
import net.orfjackal.dimdwarf.util.SingletonCache;
import net.sf.cglib.proxy.*;
import org.objenesis.*;

import javax.annotation.concurrent.Immutable;
import java.lang.reflect.Method;
import java.util.*;

@Singleton
@Immutable
public class TransparentReferenceFactory {

    private final CglibProxyFactoryCache proxyFactories = new CglibProxyFactoryCache();
    private final Provider<EntityReferenceFactory> referenceFactory;

    @Inject
    public TransparentReferenceFactory(Provider<EntityReferenceFactory> referenceFactory) {
        this.referenceFactory = referenceFactory;
    }

    /**
     * Creates a proxy for the specified entity, so that the proxy will have the same
     * external interface as the proxied entity, but the proxy itself does not need
     * to be wrapped into an {@link EntityReference}.
     */
    public TransparentReference createTransparentReference(Object entity) {
        Class<?> type = entity.getClass();
        EntityReference<?> ref = referenceFactory.get().createReference(entity);
        return newProxy(new TransparentReferenceBackend(type, ref));
    }

    /**
     * Creates a proxy from a {@link TransparentReferenceBackend} which is not yet proxied.
     * This is needed only during deserialization and should not be called elsewhere.
     */
    public TransparentReference newProxy(TransparentReferenceBackend tref) {
        Factory factory = proxyFactories.get(tref.getType$TREF());
        return (TransparentReference) factory.newInstance(new Callback[]{
                new EntityCallback(tref),
                new TransparentReferenceCallback(tref)
        });
    }


    private static class CglibProxyFactoryCache extends SingletonCache<Class<?>, Factory> {

        private final Objenesis objenesis = new ObjenesisStd();

        protected Factory newInstance(Class<?> type) {
            Enhancer e = new Enhancer();
            if (useConcreteSuperclass(type)) {
                e.setSuperclass(type);
            }
            e.setInterfaces(proxiedInterfaces(type));
            e.setCallbackTypes(new Class[]{
                    EntityCallback.class,
                    TransparentReferenceCallback.class
            });
            e.setCallbackFilter(new TransparentReferenceCallbackFilter());
            return new ConstructorIgnoringCglibProxyFactory(e.createClass(), objenesis);
        }

        private static boolean useConcreteSuperclass(Class<?> type) {
            Entity ann = type.getAnnotation(Entity.class);
            return ann != null && ann.value().equals(ProxyType.CLASS);
        }

        private static Class<?>[] proxiedInterfaces(Class<?> aClass) {
            List<Class<?>> interfaces = interfacesOf(aClass);
            assert !interfaces.contains(TransparentReference.class)
                    : aClass + " should not implement " + TransparentReference.class;
            interfaces.add(TransparentReference.class);
            return interfaces.toArray(new Class<?>[interfaces.size()]);
        }

        private static List<Class<?>> interfacesOf(Class<?> aClass) {
            List<Class<?>> interfaces = new ArrayList<Class<?>>();
            for (Class<?> c = aClass; c != null; c = c.getSuperclass()) {
                interfaces.addAll(Arrays.asList(c.getInterfaces()));
            }
            return interfaces;
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

    // TODO: add one more callback that uses 'ManagedReference.getForUpdate' or 'markForUpdate' when target method has a special annotation  

    private static class EntityCallback implements LazyLoader {

        private final TransparentReference tref;

        private EntityCallback(TransparentReference tref) {
            this.tref = tref;
        }

        public Object loadObject() throws Exception {
            return tref.getEntity$TREF();
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
