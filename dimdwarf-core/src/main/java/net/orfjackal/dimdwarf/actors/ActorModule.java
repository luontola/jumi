// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.actors;

import com.google.inject.*;
import com.google.inject.name.*;
import com.google.inject.util.Types;
import net.orfjackal.dimdwarf.context.Context;
import net.orfjackal.dimdwarf.controller.*;
import net.orfjackal.dimdwarf.mq.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

public abstract class ActorModule<M> extends PrivateModule {

    private final List<Key<ControllerRegistration>> controllers = new ArrayList<Key<ControllerRegistration>>();
    private final List<Key<ActorRegistration>> actors = new ArrayList<Key<ActorRegistration>>();
    private final Class<? extends Annotation> actorScope;

    protected final String actorName;
    protected final Class<M> messageType;
    protected final TypeLiteral<MessageSender<M>> messageSenderType;
    protected final TypeLiteral<MessageReceiver<M>> messageReceiverType;
    protected final TypeLiteral<Actor<M>> actorType;

    public ActorModule(String actorName) {
        this(actorName, ActorScoped.class);
    }

    @SuppressWarnings({"unchecked"})
    public ActorModule(String actorName, Class<? extends Annotation> actorScope) {
        this.actorName = actorName;
        this.actorScope = actorScope;

        messageType = getMessageType();
        messageSenderType = (TypeLiteral<MessageSender<M>>) TypeLiteral.get(Types.newParameterizedType(MessageSender.class, messageType));
        messageReceiverType = (TypeLiteral<MessageReceiver<M>>) TypeLiteral.get(Types.newParameterizedType(MessageReceiver.class, messageType));
        actorType = (TypeLiteral<Actor<M>>) TypeLiteral.get(Types.newParameterizedType(Actor.class, messageType));
    }

    @SuppressWarnings({"unchecked"})
    private Class<M> getMessageType() {
        ParameterizedType actorModuleType = (ParameterizedType) getClass().getGenericSuperclass();
        Type messageType = actorModuleType.getActualTypeArguments()[0];
        return (Class<M>) messageType;
    }

    public List<Key<ControllerRegistration>> getControllers() {
        return Collections.unmodifiableList(controllers);
    }

    public List<Key<ActorRegistration>> getActors() {
        return Collections.unmodifiableList(actors);
    }

    protected void bindControllerTo(Class<? extends Controller> controller) {
        checkHasAnnotation(controller, ControllerScoped.class);

        bind(controller);
        expose(controller); // allow other controllers to use the controller directly, while still making sure that it's part of this private module
        bind(Controller.class).to(controller);

        controllers.add(exposeUniqueKey(ControllerRegistration.class, controllerRegistrationProvider()));
    }

    protected void bindActorTo(Class<? extends Actor<M>> actor) {
        checkHasAnnotation(actor, actorScope);

        bind(actorType).to(actor);

        MessageQueue<M> mq = new MessageQueue<M>(actorName);
        bind(messageSenderType).toInstance(mq);
        bind(messageReceiverType).toInstance(mq);

        actors.add(exposeUniqueKey(ActorRegistration.class, actorRegistrationProvider()));
    }

    private Provider<ControllerRegistration> controllerRegistrationProvider() {
        final Provider<Controller> controller = getProvider(Controller.class);
        return new Provider<ControllerRegistration>() {
            public ControllerRegistration get() {
                return new ControllerRegistration(actorName, controller);
            }
        };
    }

    private Provider<ActorRegistration> actorRegistrationProvider() {
        final Provider<Context> context = getProvider(Key.get(Context.class, actorScope));
        final Provider<ActorRunnable> actor = getProvider(ActorRunnable.class);
        return new Provider<ActorRegistration>() {
            public ActorRegistration get() {
                return new ActorRegistration(actorName, context, actor);
            }
        };
    }

    private static void checkHasAnnotation(Class<?> target, Class<? extends Annotation> annotation) {
        if (target.getAnnotation(annotation) == null) {
            throw new IllegalArgumentException(target.getName() + " must be annotated with " + annotation.getName());
        }
    }

    private <T> Key<T> exposeUniqueKey(Class<T> type, Provider<T> provider) {
        bind(type).toProvider(provider);

        Key<T> key = Key.get(type, uniqueId());
        bind(key).to(type);
        expose(key);
        return key;
    }

    private Named uniqueId() {
        return Names.named(actorName + "/" + UUID.randomUUID().toString());
    }
}
