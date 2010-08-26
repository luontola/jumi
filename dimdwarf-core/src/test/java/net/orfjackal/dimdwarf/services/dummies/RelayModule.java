// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.services.dummies;

import com.google.inject.*;
import net.orfjackal.dimdwarf.context.Context;
import net.orfjackal.dimdwarf.controller.Controller;
import net.orfjackal.dimdwarf.mq.MessageReceiver;
import net.orfjackal.dimdwarf.services.*;

public class RelayModule extends ServiceModule {

    public RelayModule() {
        super("Relay");
    }

    protected void configure() {
        bindServiceTo(RelayService.class);
        bindControllerTo(RelayController.class);
        setMessageType(Object.class);

        bind(Context.class).to(ServiceContext.class);
    }

    @Provides
    ControllerRegistration controllerRegistration(Provider<Controller> controller) {
        return new ControllerRegistration(serviceName, controller);
    }

    @Provides
    ServiceRegistration serviceRegistration(Provider<ServiceContext> context,
                                            Provider<ServiceRunnable> service) {
        return new ServiceRegistration(serviceName, context, service);
    }

    @Provides
    ServiceRunnable service(Service service, MessageReceiver<Object> toService) {
        return new ServiceMessageLoop(service, toService);
    }
}
