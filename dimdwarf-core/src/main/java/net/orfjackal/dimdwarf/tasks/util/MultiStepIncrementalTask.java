// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.tasks.util;

import java.io.Serializable;
import java.util.*;

public class MultiStepIncrementalTask implements IncrementalTask, Serializable {
    private static final long serialVersionUID = 1L;

    // TODO: this class is not used in production code

    private final Queue<IncrementalTask> tasks = new LinkedList<IncrementalTask>();
    private final int stepsPerTask;

    public MultiStepIncrementalTask(IncrementalTask task, int stepsPerTask) {
        this.tasks.add(task);
        this.stepsPerTask = stepsPerTask;
    }

    public Collection<? extends IncrementalTask> step() {
        for (int i = 0; i < stepsPerTask && !tasks.isEmpty(); i++) {
            IncrementalTask task = tasks.poll();
            tasks.addAll(task.step());
        }
        if (tasks.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(this);
    }
}
