// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.tasks.util;

import java.io.Serializable;
import java.util.*;

public class IncrementalTaskSequence implements IncrementalTask, Serializable {
    private static final long serialVersionUID = 1L;

    private final Queue<IncrementalTask> currentStage = new LinkedList<IncrementalTask>();
    private final Queue<IncrementalTask> nextStages = new LinkedList<IncrementalTask>();

    public IncrementalTaskSequence(Collection<? extends IncrementalTask> stages) {
        this.nextStages.addAll(stages);
    }

    public Collection<? extends IncrementalTask> step() {
        IncrementalTask task = currentStage.poll();
        if (task == null) {
            task = nextStages.poll();
        }
        if (task == null) {
            return Collections.emptyList();
        }
        currentStage.addAll(task.step());
        return Arrays.asList(this);
    }
}
