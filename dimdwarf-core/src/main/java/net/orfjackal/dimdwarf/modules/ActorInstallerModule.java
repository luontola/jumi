// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.modules;

import com.google.inject.*;
import com.google.inject.multibindings.Multibinder;
import net.orfjackal.dimdwarf.actors.*;
import net.orfjackal.dimdwarf.context.*;
import net.orfjackal.dimdwarf.controller.*;

public class ActorInstallerModule extends AbstractModule {

    private final ActorModule[] actorModules;

    public ActorInstallerModule(ActorModule... actorModules) {
        this.actorModules = actorModules;
    }

    protected void configure() {
        bindScope(ControllerScoped.class, new ThreadScope(ControllerContext.class));
        bind(Context.class).annotatedWith(ControllerScoped.class).to(ControllerContext.class);

        bindScope(ActorScoped.class, new ThreadScope(ActorContext.class));
        bind(Context.class).annotatedWith(ActorScoped.class).to(ActorContext.class);

        Multibinder<ControllerRegistration> controllerBindings = Multibinder.newSetBinder(binder(), ControllerRegistration.class);
        Multibinder<ActorRegistration> actorBindings = Multibinder.newSetBinder(binder(), ActorRegistration.class);
        for (ActorModule module : actorModules) {
            install(module);
            for (Key<ControllerRegistration> key : module.getControllers()) {
                controllerBindings.addBinding().to(key);
            }
            for (Key<ActorRegistration> key : module.getActors()) {
                actorBindings.addBinding().to(key);
            }
        }
    }
}
