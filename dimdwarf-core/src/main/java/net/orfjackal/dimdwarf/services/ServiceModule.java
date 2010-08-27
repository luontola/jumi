// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.services;

import com.google.inject.*;
import com.google.inject.name.*;
import com.google.inject.util.Types;
import net.orfjackal.dimdwarf.controller.*;
import net.orfjackal.dimdwarf.mq.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.*;

public abstract class ServiceModule extends PrivateModule {

    protected final String serviceName;

    private final List<Key<ControllerRegistration>> controllers = new ArrayList<Key<ControllerRegistration>>();
    private final List<Key<ServiceRegistration>> services = new ArrayList<Key<ServiceRegistration>>();
    private final Class<? extends Annotation> serviceScope;

    public ServiceModule(String serviceName) {
        this(serviceName, ServiceScoped.class);
    }

    public ServiceModule(String serviceName, Class<? extends Annotation> serviceScope) {
        this.serviceName = serviceName;
        this.serviceScope = serviceScope;
    }

    public List<Key<ControllerRegistration>> getControllers() {
        return Collections.unmodifiableList(controllers);
    }

    public List<Key<ServiceRegistration>> getServices() {
        return Collections.unmodifiableList(services);
    }

    protected void bindControllerTo(Class<? extends Controller> controller) {
        checkHasAnnotation(controller, ControllerScoped.class);
        bind(controller);
        expose(controller); // allow other controllers to use the controller directly, while still making sure that it's part of this private module
        bind(Controller.class).to(controller);

        Key<ControllerRegistration> key = Key.get(ControllerRegistration.class, uniqueId());
        bind(key).to(ControllerRegistration.class);
        expose(key);
        controllers.add(key);
    }

    protected void bindServiceTo(Class<? extends Service> service) {
        checkHasAnnotation(service, serviceScope);
        bind(Service.class).to(service);

        Key<ServiceRegistration> key = Key.get(ServiceRegistration.class, uniqueId());
        bind(key).to(ServiceRegistration.class);
        expose(key);
        services.add(key);
    }

    private static void checkHasAnnotation(Class<?> target, Class<? extends Annotation> annotation) {
        if (target.getAnnotation(annotation) == null) {
            throw new IllegalArgumentException(target.getName() + " must be annotated with " + annotation.getName());
        }
    }

    private Named uniqueId() {
        return Names.named(serviceName + "/" + UUID.randomUUID().toString());
    }

    protected void bindMessageQueueOfType(Type messageType) {
        MessageQueue<?> mq = new MessageQueue<Object>(serviceName);
        bind(messageSenderOf(messageType)).toInstance(mq);
        bind(messageReceiverOf(messageType)).toInstance(mq);
    }

    @SuppressWarnings({"unchecked"})
    protected TypeLiteral<MessageSender<?>> messageSenderOf(Type messageType) {
        return (TypeLiteral<MessageSender<?>>) TypeLiteral.get(Types.newParameterizedType(MessageSender.class, messageType));
    }

    @SuppressWarnings({"unchecked"})
    protected TypeLiteral<MessageReceiver<?>> messageReceiverOf(Type messageType) {
        return (TypeLiteral<MessageReceiver<?>>) TypeLiteral.get(Types.newParameterizedType(MessageReceiver.class, messageType));
    }
}
