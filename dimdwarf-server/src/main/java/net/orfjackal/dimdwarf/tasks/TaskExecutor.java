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

import com.google.inject.Inject;
import com.google.inject.Provider;
import net.orfjackal.dimdwarf.context.Context;
import net.orfjackal.dimdwarf.context.ThreadContext;
import net.orfjackal.dimdwarf.tx.TransactionCoordinator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

/**
 * @author Esko Luontola
 * @since 13.9.2008
 */
public class TaskExecutor implements Executor {

    private static final Logger logger = LoggerFactory.getLogger(TaskExecutor.class);

    private final Provider<Context> contextProvider;
    private final Provider<TransactionCoordinator> txProvider;

    @Inject
    public TaskExecutor(Provider<Context> contextProvider, Provider<TransactionCoordinator> txProvider) {
        this.contextProvider = contextProvider;
        this.txProvider = txProvider;
    }

    public void execute(final Runnable command) {
        ThreadContext.runInContext(contextProvider.get(), new Runnable() {
            public void run() {
                // TODO: Refactor transaction handling out of this class:
                // - Use a nested chain of runnables for transactions, entity flushing, entity reference counting etc.
                // - Then also get rid of TransactionListener.transactionWillDeactivate()
                TransactionCoordinator tx = txProvider.get();
                try {
                    command.run();
                    tx.prepareAndCommit();
                } catch (Throwable t) {
                    logger.warn("Task failed, rolling back its transaction", t);
                    tx.rollback();
                    throw throwAsUnchecked(t);
                }
            }
        });
    }

    private static RuntimeException throwAsUnchecked(Throwable t) {
        if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        }
        if (t instanceof Error) {
            throw (Error) t;
        }
        throw new RuntimeException(t);
    }
}
