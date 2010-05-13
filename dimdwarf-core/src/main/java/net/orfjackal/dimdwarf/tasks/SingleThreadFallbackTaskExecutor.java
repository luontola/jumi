// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.tasks;

import com.google.inject.*;
import net.orfjackal.dimdwarf.tx.Retryable;
import net.orfjackal.dimdwarf.util.Exceptions;
import org.slf4j.*;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.*;

@Singleton
@ThreadSafe
public class SingleThreadFallbackTaskExecutor implements Executor {

    // FIXME: SingleThreadFallbackTaskExecutor will be removed/refactored in new architecture

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
