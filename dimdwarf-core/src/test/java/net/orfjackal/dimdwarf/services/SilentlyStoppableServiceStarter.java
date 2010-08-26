// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.services;

import com.google.inject.Inject;

import java.util.Set;

public class SilentlyStoppableServiceStarter extends ServiceStarter {

    @Inject
    public SilentlyStoppableServiceStarter(Set<ServiceRegistration> services) {
        super(services);
    }

    protected void configureThread(Thread t) {
        t.setUncaughtExceptionHandler(new HideInterruptedExceptions());
    }
}
