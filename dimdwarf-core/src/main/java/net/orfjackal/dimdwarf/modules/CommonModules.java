// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.modules;

import com.google.inject.AbstractModule;

public class CommonModules extends AbstractModule {

    protected void configure() {
        install(new TaskContextModule());
        install(new DatabaseModule());
        install(new EntityModule());
        install(new TaskSchedulingModule());
        install(new LifecycleModule());
    }
}
