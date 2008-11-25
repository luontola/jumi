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

package net.orfjackal.dimdwarf.scheduler;

import com.google.inject.*;
import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.api.EntityInfo;
import net.orfjackal.dimdwarf.entities.BindingStorage;
import net.orfjackal.dimdwarf.modules.*;
import net.orfjackal.dimdwarf.tasks.*;
import net.orfjackal.dimdwarf.tx.*;
import net.orfjackal.dimdwarf.util.*;
import org.jmock.Expectations;
import org.junit.runner.RunWith;

import java.util.logging.*;

/**
 * @author Esko Luontola
 * @since 25.11.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class TransactionalTaskSchedulerSpec extends Specification<Object> {

    private TaskSchedulerImpl scheduler;
    private Provider<BindingStorage> bindings;
    private Provider<EntityInfo> entities;
    private Provider<Transaction> tx;
    private TaskExecutor taskContext;
    private DummyClock clock;
    private Logger hideTransactionFailedLogs;

    private DummyTask task1 = new DummyTask("1");
    private DummyTask task2 = new DummyTask("2");


    public void create() {
        clock = new DummyClock();
        Injector injector = Guice.createInjector(
                new EntityModule(),
                new DatabaseModule(),
                new TaskContextModule(),
                new AbstractModule() {
                    protected void configure() {
                        bind(Clock.class).toInstance(clock);
                    }
                });
        bindings = injector.getProvider(BindingStorage.class);
        entities = injector.getProvider(EntityInfo.class);
        tx = injector.getProvider(Transaction.class);
        taskContext = injector.getInstance(TaskExecutor.class);

        scheduler = new TaskSchedulerImpl(bindings, entities, tx, clock, taskContext);

        hideTransactionFailedLogs = Logger.getLogger(TransactionFilter.class.getName());
        hideTransactionFailedLogs.setLevel(Level.OFF);
    }

    public void destroy() throws Exception {
        hideTransactionFailedLogs.setLevel(Level.ALL);
    }


    public class WhenATaskIsSubmittedInATransaction {

        public void create() {
        }

        public void theTaskIsNotQueuedUntilTheTransactionCommits() {
            taskContext.execute(new Runnable() {
                public void run() {
                    scheduler.submit(task1);
                    specify(scheduler.getQueuedTasks(), should.equal(0));
                }
            });
            specify(scheduler.getQueuedTasks(), should.equal(1));
        }

        public void theTaskIsNotQueuedIfTheTransactionRollsBack() {
            try {
                taskContext.execute(new Runnable() {
                    public void run() {
                        scheduler.submit(task1);
                        tx.get().setRollbackOnly();
                    }
                });
                fail("TransactionExpection should be thrown");
            } catch (TransactionException e) {
                specify(e.getCause() instanceof TransactionRolledbackException);
            }
            specify(scheduler.getQueuedTasks(), should.equal(0));
        }

        @SuppressWarnings("ThrowableInstanceNeverThrown")
        public void theTaskIsNotQueuedIfTheTransactionRollsBackDuringPrepare() throws Throwable {
            final TransactionParticipant failOnPrepare = mock(TransactionParticipant.class);
            checking(new Expectations() {{
                one(failOnPrepare).prepare(); will(throwException(new AssertionError("Dummy exception")));
            }});
            try {
                taskContext.execute(new Runnable() {
                    public void run() {
                        scheduler.submit(task1);
                        tx.get().join(failOnPrepare);
                    }
                });
                fail("TransactionExpection should be thrown");
            } catch (TransactionException e) {
                specify(e.getCause() instanceof AssertionError);
            }
            specify(scheduler.getQueuedTasks(), should.equal(0));
        }
    }


    // TODO: if transaction (of the task which was just taken) rolls back, the task should be rescheduled
}
