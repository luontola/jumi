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

package net.orfjackal.dimdwarf.tasks;

import com.google.inject.Provider;
import jdave.*;
import jdave.junit4.JDaveRunner;
import org.jmock.Expectations;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

import java.util.concurrent.Executor;

/**
 * @author Esko Luontola
 * @since 13.12.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class RetryingTaskExecutorSpec extends Specification<Object> {

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
