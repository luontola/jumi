// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.tasks;

import com.google.inject.Provider;
import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.tx.Retryable;
import org.jmock.Expectations;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

import java.util.concurrent.Executor;

@RunWith(JDaveRunner.class)
@Group({"fast"})
public class RetryingTaskExecutorSpec extends Specification<Object> {

    // FIXME: RetryingTaskExecutor will be removed/refactored in new architecture

    private Logger logger;
    private Executor taskContext;
    private RetryingTaskExecutor executor;

    private Runnable task;
    private RuntimeException retryableException = new RetryableException("dummy, should retry", true);
    private RuntimeException nonRetryableException = new RetryableException("dummy, should not retry", false);
    private RuntimeException failureException = new RuntimeException("dummy, should not retry");

    public void create() throws Exception {
        logger = mock(Logger.class);
        taskContext = mock(Executor.class);
        task = mock(Runnable.class);
    }


    public class WhenThePolicyIsToNotRetry {

        public void create() {
            Provider<RetryPolicy> strategy = new Provider<RetryPolicy>() {
                public RetryPolicy get() {
                    return new RetryOnRetryableExceptionsANumberOfTimes(0);
                }
            };
            executor = new RetryingTaskExecutor(taskContext, strategy, logger);
        }

        public void aSuccessfulTaskIsExecutedOnce() {
            checking(new Expectations() {{
                one(taskContext).execute(task);
            }});
            executor.execute(task);
        }

        public void aFailingTaskIsNotRetried() {
            checking(new Expectations() {{
                one(taskContext).execute(task); will(throwException(retryableException));
            }});
            specify(new Block() {
                public void run() throws Throwable {
                    executor.execute(task);
                }
            }, should.raise(GivenUpOnTaskException.class));
        }
    }

    public class WhenThePolicyIsToRetryANumberOfTimes {

        public void create() {
            Provider<RetryPolicy> strategy = new Provider<RetryPolicy>() {
                public RetryPolicy get() {
                    return new RetryOnRetryableExceptionsANumberOfTimes(1);
                }
            };
            executor = new RetryingTaskExecutor(taskContext, strategy, logger);
        }

        public void aSuccessfulTaskIsExecutedOnce() {
            checking(new Expectations() {{
                one(taskContext).execute(task);
            }});
            executor.execute(task);
        }


        public void aFailingTaskIsRetriedUntilItPasses() {
            checking(new Expectations() {{
                one(taskContext).execute(task); will(throwException(retryableException));
                one(logger).info("Retrying a failed task");
                one(taskContext).execute(task);
            }});
            executor.execute(task);
        }

        public void aFailingTaskIsNotRetriedIfItFailsTooManyTimes() {
            checking(new Expectations() {{
                one(taskContext).execute(task); will(throwException(retryableException));
                one(logger).info("Retrying a failed task");
                one(taskContext).execute(task); will(throwException(retryableException));
            }});
            specify(new Block() {
                public void run() throws Throwable {
                    executor.execute(task);
                }
            }, should.raise(GivenUpOnTaskException.class));
        }

        public void nonRetryableExceptionsAreNotRetried() {
            checking(new Expectations() {{
                one(taskContext).execute(task); will(throwException(nonRetryableException));
            }});
            specify(new Block() {
                public void run() throws Throwable {
                    executor.execute(task);
                }
            }, should.raise(GivenUpOnTaskException.class));
        }

        public void failureExceptionsAreNotRetried() {
            checking(new Expectations() {{
                one(taskContext).execute(task); will(throwException(failureException));
            }});
            specify(new Block() {
                public void run() throws Throwable {
                    executor.execute(task);
                }
            }, should.raise(GivenUpOnTaskException.class));
        }
    }


    private static class RetryableException extends RuntimeException implements Retryable {
        private final boolean retry;

        public RetryableException(String message, boolean retry) {
            super(message);
            this.retry = retry;
        }

        public boolean mayBeRetried() {
            return retry;
        }
    }
}
