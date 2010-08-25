// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.services;

import net.orfjackal.dimdwarf.context.*;

public class ServiceRunner implements Runnable {

    private final ServiceRegistration service;

    public ServiceRunner(ServiceRegistration service) {
        this.service = service;
    }

    public void run() {
        Context context = service.getContext().get();
        ThreadContext.runInContext(context, new Runnable() {
            public void run() {
                service.getService().get().run();
            }
        });
    }
}
