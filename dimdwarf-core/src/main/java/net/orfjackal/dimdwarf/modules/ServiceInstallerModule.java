// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.modules;

import com.google.inject.*;
import com.google.inject.multibindings.Multibinder;
import net.orfjackal.dimdwarf.context.ThreadScope;
import net.orfjackal.dimdwarf.controller.*;
import net.orfjackal.dimdwarf.services.*;

public class ServiceInstallerModule extends AbstractModule {

    private final ServiceModule[] serviceModules;

    public ServiceInstallerModule(ServiceModule... serviceModules) {
        this.serviceModules = serviceModules;
    }

    protected void configure() {
        bindScope(ControllerScoped.class, new ThreadScope(ControllerContext.class));
        bindScope(ServiceScoped.class, new ThreadScope(ServiceContext.class));

        Multibinder<ControllerRegistration> controllerBindings = Multibinder.newSetBinder(binder(), ControllerRegistration.class);
        Multibinder<ServiceRegistration> serviceBindings = Multibinder.newSetBinder(binder(), ServiceRegistration.class);
        for (ServiceModule module : serviceModules) {
            install(module);
            for (Key<ControllerRegistration> key : module.getControllers()) {
                controllerBindings.addBinding().to(key);
            }
            for (Key<ServiceRegistration> key : module.getServices()) {
                serviceBindings.addBinding().to(key);
            }
        }
    }
}
