// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.services;

import org.apache.mina.util.ConcurrentHashSet;

import javax.annotation.concurrent.ThreadSafe;
import java.util.*;

@ThreadSafe
public class ServiceStarter {

    private final Set<ServiceRegistration> services;
    private final Set<Thread> serviceThreads = new ConcurrentHashSet<Thread>();

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

    public void start() {
        for (ServiceRegistration service : services) {
            Thread t = new Thread(new ServiceRunner(service), service.getName());
            configureThread(t);
            t.start();
            serviceThreads.add(t);
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void configureThread(Thread t) {
        // override to for example configure in tests
    }

    public void stop() throws InterruptedException {
        for (Thread t : serviceThreads) {
            t.interrupt();
            t.join();
        }
    }
}
