// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.modules;

import com.google.inject.Provides;
import net.orfjackal.dimdwarf.actors.*;
import net.orfjackal.dimdwarf.controller.*;
import net.orfjackal.dimdwarf.mq.MessageReceiver;
import org.slf4j.*;

import java.util.*;

public class ControllerModule extends ActorModule<Object> {

    private static Logger logger = LoggerFactory.getLogger(ControllerModule.class);

    public ControllerModule() {
        super("Controller", ControllerScoped.class);
    }

    protected void configure() {
        bindActorTo(ControllerHub.class);

        bind(messageSenderType).annotatedWith(Hub.class).to(messageSenderType);
        expose(messageSenderType).annotatedWith(Hub.class);
    }

    @Provides
    ActorRunnable actor(ControllerHub actor, MessageReceiver<Object> toActor, Set<ControllerRegistration> controllerRegs) {
        registerControllers(actor, controllerRegs);
        return new ActorMessageLoop(actor, toActor);
    }

    private static void registerControllers(ControllerHub hub, Set<ControllerRegistration> controllerRegs) {
        for (ControllerRegistration reg : preventTemporalCoupling(controllerRegs)) {
            Controller controller = reg.getController().get();
            logger.info("Registering controller \"{}\" of type {}", reg.getName(), controller.getClass().getName());
            hub.addController(controller);
        }
    }

    private static List<ControllerRegistration> preventTemporalCoupling(Set<ControllerRegistration> deterministicOrder) {
        // In order to detect any temporal coupling between the order in which
        // the controllers are called, we will randomize their order, so that
        // the system cannot rely on the controller invocation order. Flakiness
        // in end-to-end tests may be a symptom of controller order dependency,
        // in which case you should check the logs that in which order the
        // controllers were registered.
        List<ControllerRegistration> randomOrder = new ArrayList<ControllerRegistration>(deterministicOrder);
        Collections.shuffle(randomOrder);
        return randomOrder;
    }
}
