// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.modules;

import com.google.inject.name.Names;
import net.orfjackal.dimdwarf.actors.ActorModule;
import net.orfjackal.dimdwarf.net.*;

public class NetworkModule extends ActorModule<NetworkMessage> {

    private final int port;

    public NetworkModule(int port) {
        super("Network");
        this.port = port;
    }

    protected void configure() {
        bindControllerTo(NetworkController.class);
        bindActorTo(NetworkActor.class);

        bindConstant().annotatedWith(Names.named("port")).to(port);
    }
}
