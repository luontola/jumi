// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.actors;

import com.google.inject.Inject;

import java.util.Set;

public class SilentlyStoppableActorStarter extends ActorStarter {

    @Inject
    public SilentlyStoppableActorStarter(Set<ActorRegistration> actors) {
        super(actors);
    }

    protected void configureThread(Thread t) {
        t.setUncaughtExceptionHandler(new HideInterruptedExceptions());
    }
}
