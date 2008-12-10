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
import net.orfjackal.dimdwarf.api.TaskScheduler;
import net.orfjackal.dimdwarf.gc.*;
import net.orfjackal.dimdwarf.gc.cms.ConcurrentMarkSweepCollector;
import net.orfjackal.dimdwarf.tasks.TaskExecutor;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.concurrent.locks.*;

/**
 * @author Esko Luontola
 * @since 10.12.2008
 */
@Singleton
public class CmsCollectorManager implements GarbageCollectorManager {

    private static final int STEPS_PER_TASK = 10;

    private final Provider<ConcurrentMarkSweepCollector<BigInteger>> cms;
    private final Provider<TaskScheduler> scheduler;
    private final TaskExecutor taskContext;
    private final Lock lock = new ReentrantLock();
    private final Condition gcFinished = lock.newCondition();

    @Inject
    public CmsCollectorManager(Provider<ConcurrentMarkSweepCollector<BigInteger>> cms,
                               Provider<TaskScheduler> scheduler,
                               TaskExecutor taskContext) {
        this.cms = cms;
        this.taskContext = taskContext;
        this.scheduler = scheduler;
    }

    public void runGarbageCollector() throws InterruptedException {
        // XXX: MultiStepIncrementalTask is needed because running the collector generates more garbage than it uses.
        // A better solution would be to avoid using the TaskScheduler, because each scheduled task creates at least one new entity.

        lock.lock();
        try {
            taskContext.execute(new Runnable() {
                public void run() {
                    scheduler.get().submit(
                            new IncrementalTaskRunner(
                                    new MultiStepIncrementalTask(
                                            new IncrementalTaskSequence(cms.get().getCollectorStagesToExecute()),
                                            STEPS_PER_TASK),
                                    new OnCollectionFinished()));
                }
            });
            gcFinished.await();
        } finally {
            lock.unlock();
        }
    }

    private void notifyCollectionFinished() {
        lock.lock();
        try {
            gcFinished.signal();
        } finally {
            lock.unlock();
        }
    }

    private static class OnCollectionFinished implements Runnable, Serializable {
        private static final long serialVersionUID = 1L;

        @Inject public transient CmsCollectorManager manager;

        public void run() {
            manager.notifyCollectionFinished();
        }
    }
}