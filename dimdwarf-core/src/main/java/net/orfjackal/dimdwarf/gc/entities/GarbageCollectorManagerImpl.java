// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.gc.entities;

import com.google.inject.*;
import net.orfjackal.dimdwarf.api.Entity;
import net.orfjackal.dimdwarf.api.internal.EntityObject;
import net.orfjackal.dimdwarf.entities.BindingRepository;
import net.orfjackal.dimdwarf.gc.GarbageCollector;
import net.orfjackal.dimdwarf.tasks.RetryingTaskContext;
import net.orfjackal.dimdwarf.tasks.util.*;

import javax.annotation.concurrent.ThreadSafe;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Esko Luontola
 * @since 10.12.2008
 */
@Singleton
@ThreadSafe
public class GarbageCollectorManagerImpl implements GarbageCollectorManager {

    private static final String WORKER_BINDING = GarbageCollectorManagerImpl.class.getName() + ".worker";

    private final Provider<GarbageCollector<BigInteger>> collector;
    private final Provider<BindingRepository> bindings;
    private final Executor taskContext;

    @Inject
    public GarbageCollectorManagerImpl(Provider<GarbageCollector<BigInteger>> collector,
                                       Provider<BindingRepository> bindings,
                                       @RetryingTaskContext Executor taskContext) {
        this.collector = collector;
        this.bindings = bindings;
        this.taskContext = taskContext;
    }

    public void runGarbageCollector() {
        initGcWorker();
        boolean done;
        do {
            done = stepGcWorker();
        } while (!done);
    }

    private void initGcWorker() {
        taskContext.execute(new Runnable() {
            public void run() {
                IncrementalTask gc = new IncrementalTaskSequence(collector.get().getCollectorStagesToExecute());
                GcWorker w = new GcWorker(gc);
                bindings.get().update(WORKER_BINDING, w);
            }
        });
    }

    private boolean stepGcWorker() {
        final AtomicBoolean done = new AtomicBoolean(false);
        taskContext.execute(new Runnable() {
            public void run() {
                GcWorker w = (GcWorker) bindings.get().read(WORKER_BINDING);
                w.step();
                done.set(w.isDone());
            }
        });
        return done.get();
    }


    @Entity
    private static class GcWorker implements EntityObject, Serializable {

        private final Queue<IncrementalTask> workQueue = new ArrayDeque<IncrementalTask>();

        public GcWorker(IncrementalTask gc) {
            workQueue.add(gc);
        }

        public void step() {
            IncrementalTask task = workQueue.poll();
            workQueue.addAll(task.step());
        }

        public boolean isDone() {
            return workQueue.isEmpty();
        }
    }

    // TODO: make garbage collection non-persisted
    // TODO: unit test this class independently of the GC implementation
}
