/*
 * This file is part of Dimdwarf Application Server <http://dimdwarf.sourceforge.net/>
 *
 * Copyright (c) 2008, Esko Luontola. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *
 *     * Neither the name of the copyright holder nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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

    // TODO: unit test this class independently of the GC implementation
}
