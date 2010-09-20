// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.actors;

import com.google.inject.Inject;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;

@NotThreadSafe
public class ActorStarter {

    private final Set<ActorRegistration> actors;
    private final Set<Thread> actorThreads = new HashSet<Thread>();

    @Inject
    public ActorStarter(Set<ActorRegistration> actors) {
        checkForDuplicateNames(actors);
        this.actors = actors;
    }

    private static void checkForDuplicateNames(Set<ActorRegistration> actors) {
        Set<String> names = new HashSet<String>();
        for (ActorRegistration actor : actors) {
            String name = actor.getName();
            if (names.contains(name)) {
                throw new IllegalArgumentException("Duplicate actor names are not allowed: " + name);
            }
            names.add(name);
        }
    }

    public void start() throws InterruptedException {
        List<ActorRunner> runners = new ArrayList<ActorRunner>();
        for (ActorRegistration actor : actors) {
            ActorRunner r = new ActorRunner(actor);
            startNewThread(r, actor.getName());
            runners.add(r);
        }
        waitForActorsToStart(runners);
    }

    private void startNewThread(Runnable target, String name) {
        Thread t = new Thread(target, name);
        configureThread(t);
        t.start();
        actorThreads.add(t);
    }

    private static void waitForActorsToStart(List<ActorRunner> runners) throws InterruptedException {
        for (ActorRunner runner : runners) {
            runner.awaitStarted();
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void configureThread(Thread t) {
        // override to for example set an UncaughtExceptionHandler
    }

    public void stop() throws InterruptedException {
        for (Thread t : actorThreads) {
            t.interrupt();
            t.join();
        }
    }
}
