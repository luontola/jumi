/*
 * Copyright (c) 2008, Esko Luontola. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
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

package net.orfjackal.dimdwarf.tx;

import static net.orfjackal.dimdwarf.tx.TransactionStatus.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Esko Luontola
 * @since 15.8.2008
 */
public class TransactionImpl implements Transaction, TransactionCoordinator {
    private static final Logger logger = LoggerFactory.getLogger(TransactionImpl.class);

    private final Object lock = new Object();
    private final Collection<TransactionParticipant> participants = new ConcurrentLinkedQueue<TransactionParticipant>();
    private volatile TransactionStatus status = ACTIVE;
    private volatile boolean rollbackOnly = false;

    public Transaction getTransaction() {
        return this;
    }

    public void join(TransactionParticipant p) {
        mustBeActive();
        if (!participants.contains(p)) {
            p.joinedTransaction(getTransaction());
            participants.add(p);
        }
    }

    public void prepare() throws TransactionFailedException {
        changeStatus(ACTIVE, PREPARING);
        try {
            checkIsNotRollbackOnly();
            prepareAllParticipants();
            checkIsNotRollbackOnly();
            changeStatus(PREPARING, PREPARE_OK);
        } catch (Throwable t) {
            changeStatus(PREPARING, PREPARE_FAILED);
            throw new TransactionFailedException("Prepare failed", t);
        }
    }

    public void commit() {
        checkIsNotRollbackOnly();
        changeStatus(PREPARE_OK, COMMITTING);
        if (commitAllParticipants()) {
            changeStatus(COMMITTING, COMMIT_OK);
        } else {
            changeStatus(COMMITTING, COMMIT_FAILED);
        }
    }

    public void rollback() {
        TransactionStatus[] from = {ACTIVE, PREPARE_OK, PREPARE_FAILED};
        changeStatus(from, ROLLBACKING);
        if (rollbackAllParticipants()) {
            changeStatus(ROLLBACKING, ROLLBACK_OK);
        } else {
            changeStatus(ROLLBACKING, ROLLBACK_FAILED);
        }
    }

    private void prepareAllParticipants() throws Throwable {
        for (TransactionParticipant p : participants) {
            p.prepare(getTransaction());
        }
    }

    private boolean commitAllParticipants() {
        boolean allSucceeded = true;
        for (TransactionParticipant p : participants) {
            try {
                p.commit(getTransaction());
            } catch (Throwable t) {
                allSucceeded = false;
                logger.error("Commit failed for participant " + p, t);
            }
        }
        return allSucceeded;
    }

    private boolean rollbackAllParticipants() {
        boolean allSucceeded = true;
        for (TransactionParticipant p : participants) {
            try {
                p.rollback(getTransaction());
            } catch (Throwable t) {
                allSucceeded = false;
                logger.error("Rollback failed for participant " + p, t);
            }
        }
        return allSucceeded;
    }

    public int getParticipants() {
        return participants.size();
    }

    public TransactionStatus getStatus() {
        return status;
    }

    private void changeStatus(TransactionStatus from, TransactionStatus to) {
        synchronized (lock) {
            if (!status.equals(from)) {
                throw new IllegalStateException("Expected " + from + " but was " + status);
            }
            status = to;
        }
    }

    private void changeStatus(TransactionStatus[] fromAny, TransactionStatus to) {
        synchronized (lock) {
            for (TransactionStatus from : fromAny) {
                if (status.equals(from)) {
                    status = to;
                    return;
                }
            }
            throw new IllegalStateException("Expected one of " + Arrays.toString(fromAny) + " but was " + status);
        }
    }

    public boolean isActive() {
        return status.equals(ACTIVE);
    }

    public void mustBeActive() throws IllegalStateException {
        if (!isActive()) {
            throw new IllegalStateException("Transaction not active");
        }
    }

    public boolean isRollbackOnly() {
        return rollbackOnly;
    }

    public void setRollbackOnly() {
        rollbackOnly = true;
    }

    private void checkIsNotRollbackOnly() {
        if (rollbackOnly) {
            throw new IllegalStateException("Marked for rollback");
        }
    }
}
