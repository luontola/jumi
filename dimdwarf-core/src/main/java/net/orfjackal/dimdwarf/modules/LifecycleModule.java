// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.modules;

import com.google.inject.*;
import net.orfjackal.dimdwarf.events.SystemLifecycleListener;
import net.orfjackal.dimdwarf.scheduler.TaskSchedulingLifecycleManager;

/**
 * @author Esko Luontola
 * @since 28.11.2008
 */
public class LifecycleModule extends AbstractModule {

    protected void configure() {
    }

    @Provides
    SystemLifecycleListener[] lifecycleListeners(TaskSchedulingLifecycleManager listener1) {
        return new SystemLifecycleListener[]{listener1};
    }
}
