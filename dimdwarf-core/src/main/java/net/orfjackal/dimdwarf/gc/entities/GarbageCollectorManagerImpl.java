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
import net.orfjackal.dimdwarf.tasks.TaskExecutor;

import javax.annotation.concurrent.ThreadSafe;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.concurrent.locks.*;

/**
 * @author Esko Luontola
 * @since 10.12.2008
 */
@Singleton
@ThreadSafe
public class GarbageCollectorManagerImpl implements GarbageCollectorManager {

    private final Provider<GarbageCollector<BigInteger>> collector;
    private final Provider<TaskScheduler> scheduler;
    private final TaskExecutor taskContext;

    private final Lock lock = new ReentrantLock(true);
    private final Condition collectionFinished = lock.newCondition();

    @Inject
    public GarbageCollectorManagerImpl(Provider<GarbageCollector<BigInteger>> collector,
                                       Provider<TaskScheduler> scheduler,
                                       TaskExecutor taskContext) {
        this.collector = collector;
        this.scheduler = scheduler;
        this.taskContext = taskContext;
    }

    public void runGarbageCollector() throws InterruptedException {
        // TODO: create RetryingTaskExecutor (with injectable retry strategy) and do not use TaskScheduler here, to avoid creating entities through running GC
        lock.lock();
        try {
            taskContext.execute(new Runnable() {
                public void run() {
                    scheduler.get().submit(
                            new IncrementalTaskRunner(
                                    new IncrementalTaskSequence(collector.get().getCollectorStagesToExecute()),
                                    new OnCollectionFinished()));
                }
            });
            collectionFinished.await();
        } finally {
            lock.unlock();
        }
    }

    private void signalCollectionFinished() {
        lock.lock();
        try {
            collectionFinished.signal();
        } finally {
            lock.unlock();
        }
    }

    private static class OnCollectionFinished implements Runnable, Serializable {
        private static final long serialVersionUID = 1L;

        @Inject public transient GarbageCollectorManagerImpl manager;

        public void run() {
            manager.signalCollectionFinished();
        }
    }

    // TODO: unit test this class independently of the GC implementation
}
