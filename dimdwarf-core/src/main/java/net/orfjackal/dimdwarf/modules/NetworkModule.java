// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.modules;

import com.google.inject.Provides;
import com.google.inject.name.Names;
import net.orfjackal.dimdwarf.actors.*;
import net.orfjackal.dimdwarf.mq.MessageReceiver;
import net.orfjackal.dimdwarf.net.*;

public class NetworkModule extends ActorModule<NetworkMessage> {

    private final int port;

    public NetworkModule(int port) {
        super("Network");
        this.port = port;
    }

    protected void configure() {
        bindConstant().annotatedWith(Names.named("port")).to(port);

        bindControllerTo(NetworkController.class);
        bindActorTo(NetworkActor.class);
        bindMessageQueueOfType(NetworkMessage.class);
    }

    @Provides
    ActorRunnable actor(Actor<NetworkMessage> actor, MessageReceiver<NetworkMessage> toActor) {
        return new ActorMessageLoop<NetworkMessage>(actor, toActor);
    }
}
