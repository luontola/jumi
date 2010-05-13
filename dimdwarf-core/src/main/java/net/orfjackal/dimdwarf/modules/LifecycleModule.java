// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.modules;

import com.google.inject.*;
import net.orfjackal.dimdwarf.events.SystemLifecycleListener;
import net.orfjackal.dimdwarf.scheduler.TaskSchedulingLifecycleManager;

public class LifecycleModule extends AbstractModule {

    protected void configure() {
    }

    @Provides
    SystemLifecycleListener[] lifecycleListeners(TaskSchedulingLifecycleManager listener1) {
        return new SystemLifecycleListener[]{listener1};
    }
}
