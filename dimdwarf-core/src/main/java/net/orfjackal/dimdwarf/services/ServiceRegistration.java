// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.services;

import com.google.inject.Provider;
import net.orfjackal.dimdwarf.context.Context;

public class ServiceRegistration {

    private final String name;
    private final Provider<? extends Context> context;
    private final Provider<? extends ServiceRunnable> service;

    public ServiceRegistration(String name,
                               Provider<? extends Context> context,
                               Provider<? extends ServiceRunnable> service) {
        this.name = name;
        this.context = context;
        this.service = service;
    }

    public String getName() {
        return name;
    }

    public Provider<? extends Context> getContext() {
        return context;
    }

    public Provider<? extends ServiceRunnable> getService() {
        return service;
    }
}
