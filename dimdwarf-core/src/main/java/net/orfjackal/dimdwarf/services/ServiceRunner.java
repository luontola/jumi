// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.services;

import net.orfjackal.dimdwarf.context.*;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.CountDownLatch;

@ThreadSafe
public class ServiceRunner implements Runnable {

    private final ServiceRegistration service;
    private final CountDownLatch started = new CountDownLatch(1);

    public ServiceRunner(ServiceRegistration service) {
        this.service = service;
    }

    public void awaitStarted() throws InterruptedException {
        started.await();
    }

    public void run() {
        Context context = service.getContext().get();
        ThreadContext.runInContext(context, new Runnable() {
            public void run() {
                ServiceRunnable sr = service.getService().get();
                sr.start();
                started.countDown();
                sr.run();
            }
        });
    }
}
