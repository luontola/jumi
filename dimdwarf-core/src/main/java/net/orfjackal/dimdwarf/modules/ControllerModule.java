// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.modules;

import com.google.inject.*;
import net.orfjackal.dimdwarf.controller.*;
import net.orfjackal.dimdwarf.mq.*;
import net.orfjackal.dimdwarf.services.*;

import java.util.Set;

public class ControllerModule extends ServiceModule {

    public ControllerModule() {
        super("Controller");
    }

    protected void configure() {
        bindServiceTo(ControllerHub.class);

        MessageQueue<Object> toHub = new MessageQueue<Object>();
        bind(messageSenderOf(Object.class)).annotatedWith(Hub.class).toInstance(toHub);
        bind(messageReceiverOf(Object.class)).annotatedWith(Hub.class).toInstance(toHub);
        expose(messageSenderOf(Object.class)).annotatedWith(Hub.class);
    }

    @Provides
    ServiceRegistration serviceRegistration(Provider<ControllerContext> context, Provider<Runnable> service) {
        return new ServiceRegistration(serviceName, context, service);
    }

    @Provides
    Runnable service(ControllerHub hub, @Hub MessageReceiver<Object> toHub, Set<ControllerRegistration> controllerRegs) {
        // TODO: move registration out of modules
        // TODO: randomize the controller order to prevent temporal coupling
        for (ControllerRegistration reg : controllerRegs) {
            System.out.println("Add controller " + reg.getName());
            hub.addController(reg.getController().get());
        }
        return new ServiceMessageLoop(hub, toHub);
    }
}
