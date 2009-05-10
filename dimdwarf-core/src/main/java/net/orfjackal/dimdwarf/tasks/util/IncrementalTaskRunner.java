// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.tasks.util;

import com.google.inject.Inject;
import net.orfjackal.dimdwarf.api.TaskScheduler;

import java.io.Serializable;
import java.util.*;

/**
 * @author Esko Luontola
 * @since 10.12.2008
 */
public class IncrementalTaskRunner implements Runnable, Serializable {
    private static final long serialVersionUID = 1L;

    // TODO: this class is not used in production code

    @Inject public transient TaskScheduler scheduler;
    private final Queue<IncrementalTask> tasks = new LinkedList<IncrementalTask>();
    private final Runnable onFinished;

    public IncrementalTaskRunner(IncrementalTask task, Runnable onFinished) {
        this.tasks.add(task);
        this.onFinished = onFinished;
    }

    public void run() {
        IncrementalTask task = tasks.poll();
        if (task == null) {
            onFinished.run();
        } else {
            tasks.addAll(task.step());
            scheduler.submit(this);
        }
    }
}
