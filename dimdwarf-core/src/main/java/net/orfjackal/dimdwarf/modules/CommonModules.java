// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.modules;

import com.google.inject.AbstractModule;

/**
 * @author Esko Luontola
 * @since 13.9.2008
 */
public class CommonModules extends AbstractModule {

    protected void configure() {
        install(new TaskContextModule());
        install(new DatabaseModule());
        install(new EntityModule());
        install(new TaskSchedulingModule());
        install(new LifecycleModule());
    }
}
