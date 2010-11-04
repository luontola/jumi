// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.actors.dummies;

import net.orfjackal.dimdwarf.actors.ActorModule;

public class RelayModule extends ActorModule<Object> {

    public RelayModule() {
        super("Relay");
    }

    protected void configure() {
        bindControllerTo(RelayController.class);
        bindActorTo(RelayActor.class);
    }
}
