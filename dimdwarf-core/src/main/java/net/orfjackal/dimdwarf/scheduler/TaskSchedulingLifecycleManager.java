// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.scheduler;

import com.google.inject.Inject;
import net.orfjackal.dimdwarf.events.SystemLifecycleListener;

import javax.annotation.concurrent.Immutable;

@Immutable
public class TaskSchedulingLifecycleManager implements SystemLifecycleListener {

    private final TaskSchedulerImpl taskScheduler;
    private final TaskThreadPool taskThreadPool;

    @Inject
    public TaskSchedulingLifecycleManager(TaskSchedulerImpl taskScheduler, TaskThreadPool taskThreadPool) {
        this.taskScheduler = taskScheduler;
        this.taskThreadPool = taskThreadPool;
    }

    public void onStartup() {
        taskScheduler.start();
        taskThreadPool.start();
    }

    public void onShutdown() {
        taskThreadPool.shutdown();
    }
}
