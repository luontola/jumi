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

/**
 * @author Esko Luontola
 * @since 13.12.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class RetryingTaskExecutorSpec extends Specification<Object> {

    private Logger logger;
    private TaskExecutor taskContext;
    private RetryingTaskExecutor executor;

    private Runnable task;
    private RuntimeException retryableException = new RuntimeException("dummy exception");

    public void create() throws Exception {
        logger = dummy(Logger.class);
        taskContext = mock(TaskExecutor.class);
        task = mock(Runnable.class);
    }

    public class WhenThePolicyIsToNotRetry {

        public void create() {
            Provider<RetryPolicy> strategy = new Provider<RetryPolicy>() {
                public RetryPolicy get() {
                    return new DoNotRetry();
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
            }, should.raise(RuntimeException.class));
        }
    }

    public class WhenThePolicyIsToRetryANumberOfTimes {

        public void create() {
            Provider<RetryPolicy> strategy = new Provider<RetryPolicy>() {
                public RetryPolicy get() {
                    return new RetryANumberOfTimes(1);
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
                one(taskContext).execute(task);
            }});
            executor.execute(task);
        }

        public void aFailingTaskIsNotRetriedIfItFailsTooManyTimes() {
            checking(new Expectations() {{
                one(taskContext).execute(task); will(throwException(retryableException));
                one(taskContext).execute(task); will(throwException(retryableException));
            }});
            specify(new Block() {
                public void run() throws Throwable {
                    executor.execute(task);
                }
            }, should.raise(RuntimeException.class));
        }
    }

}
