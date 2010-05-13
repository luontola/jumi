// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

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
