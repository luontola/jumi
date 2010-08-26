// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.services;

import com.google.inject.Inject;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;

@NotThreadSafe
public class ServiceStarter {

    private final Set<ServiceRegistration> services;
    private final Set<Thread> serviceThreads = new HashSet<Thread>();

    @Inject
    public ServiceStarter(Set<ServiceRegistration> services) {
        checkForDuplicateNames(services);
        this.services = services;
    }

    private static void checkForDuplicateNames(Set<ServiceRegistration> services) {
        Set<String> names = new HashSet<String>();
        for (ServiceRegistration service : services) {
            String name = service.getName();
            if (names.contains(name)) {
                throw new IllegalArgumentException("Duplicate service names are not allowed: " + name);
            }
            names.add(name);
        }
    }

    public void start() throws InterruptedException {
        List<ServiceRunner> runners = new ArrayList<ServiceRunner>();
        for (ServiceRegistration service : services) {
            ServiceRunner r = new ServiceRunner(service);
            startNewThread(r, service.getName());
            runners.add(r);
        }
        waitForServicesToStart(runners);
    }

    private void startNewThread(Runnable target, String name) {
        Thread t = new Thread(target, name);
        configureThread(t);
        t.start();
        serviceThreads.add(t);
    }

    private static void waitForServicesToStart(List<ServiceRunner> runners) throws InterruptedException {
        for (ServiceRunner runner : runners) {
            runner.awaitStarted();
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void configureThread(Thread t) {
        // override to for example set an UncaughtExceptionHandler
    }

    public void stop() throws InterruptedException {
        for (Thread t : serviceThreads) {
            t.interrupt();
            t.join();
        }
    }
}
