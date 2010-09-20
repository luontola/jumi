// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.actors;

import com.google.inject.Provider;
import net.orfjackal.dimdwarf.context.Context;

public class ActorRegistration {

    private final String name;
    private final Provider<? extends Context> context;
    private final Provider<? extends ActorRunnable> actor;

    public ActorRegistration(String name,
                               Provider<? extends Context> context,
                               Provider<? extends ActorRunnable> actor) {
        this.name = name;
        this.context = context;
        this.actor = actor;
    }

    public String getName() {
        return name;
    }

    public Provider<? extends Context> getContext() {
        return context;
    }

    public Provider<? extends ActorRunnable> getActor() {
        return actor;
    }
}
