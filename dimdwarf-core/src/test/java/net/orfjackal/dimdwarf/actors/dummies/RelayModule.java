// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.actors.dummies;

import com.google.inject.Provides;
import net.orfjackal.dimdwarf.actors.*;
import net.orfjackal.dimdwarf.mq.MessageReceiver;

public class RelayModule extends ActorModule {

    public RelayModule() {
        super("Relay");
    }

    protected void configure() {
        bindControllerTo(RelayController.class);
        bindActorTo(RelayActor.class);
        bindMessageQueueOfType(Object.class);
    }

    @Provides
    ActorRunnable actor(Actor<Object> actor, MessageReceiver<Object> toActor) {
        return new ActorMessageLoop<Object>(actor, toActor);
    }
}
