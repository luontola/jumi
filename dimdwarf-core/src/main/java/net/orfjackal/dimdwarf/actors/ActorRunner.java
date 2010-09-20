// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.actors;

import net.orfjackal.dimdwarf.context.*;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.CountDownLatch;

@ThreadSafe
public class ActorRunner implements Runnable {

    private final ActorRegistration actor;
    private final CountDownLatch started = new CountDownLatch(1);

    public ActorRunner(ActorRegistration actor) {
        this.actor = actor;
    }

    public void awaitStarted() throws InterruptedException {
        started.await();
    }

    public void run() {
        Context context = actor.getContext().get();
        ThreadContext.runInContext(context, new Runnable() {
            public void run() {
                ActorRunnable r = actor.getActor().get();
                r.start();
                started.countDown();
                r.run();
            }
        });
    }
}
