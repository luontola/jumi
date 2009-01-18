/*
 * This file is part of Dimdwarf Application Server <http://dimdwarf.sourceforge.net/>
 *
 * Copyright (c) 2008-2009, Esko Luontola. All Rights Reserved.
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

package net.orfjackal.dimdwarf.tasks;

import com.google.inject.*;
import net.orfjackal.dimdwarf.tx.Retryable;
import net.orfjackal.dimdwarf.util.Exceptions;
import org.slf4j.*;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.*;

/**
 * @author Esko Luontola
 * @since 18.1.2009
 */
@Singleton
@ThreadSafe
public class SingleThreadFallbackTaskExecutor implements Executor {
    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(SingleThreadFallbackTaskExecutor.class);
    private final Logger logger;

    private final Executor taskContext;
    private final ReadWriteLock threadingModeLock = new ReentrantReadWriteLock();

    @Inject
    public SingleThreadFallbackTaskExecutor(@RetryingTaskContext Executor taskContext) {
        this(taskContext, DEFAULT_LOGGER);
    }

    public SingleThreadFallbackTaskExecutor(Executor taskContext, Logger logger) {
        this.taskContext = taskContext;
        this.logger = logger;
    }

    public void execute(Runnable command) {
        try {
            executeInParallel(command);
        } catch (Throwable t) {
            if (shouldRetry(t)) {
                executeSingleThreadedly(command, t);
            } else {
                throw Exceptions.throwAsUnchecked(t);
            }
        }
    }

    private void executeInParallel(Runnable command) {
        Lock lock = multiThreadedMode();
        lock.lock();
        try {
            taskContext.execute(command);
        } finally {
            lock.unlock();
        }
    }

    private void executeSingleThreadedly(Runnable command, Throwable t) {
        Lock lock = singleThreadedMode();
        lock.lock();
        try {
            logger.info("Retrying task in single-threaded mode: " + command, t);
            taskContext.execute(command);
        } finally {
            lock.unlock();
        }
    }

    private Lock multiThreadedMode() {
        return threadingModeLock.readLock();
    }

    private Lock singleThreadedMode() {
        return threadingModeLock.writeLock();
    }

    private boolean shouldRetry(Throwable t) {
        for (; t != null; t = t.getCause()) {
            if (t instanceof Retryable) {
                return ((Retryable) t).mayBeRetried();
            }
        }
        return false;
    }
}
