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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Esko Luontola
 * @since 15.8.2008
 */
public class Transaction {
    private static final Logger logger = LoggerFactory.getLogger(Transaction.class);

    private final Object lock = new Object();
    private final Collection<TransactionParticipant> participants = new ConcurrentLinkedQueue<TransactionParticipant>();
    private volatile Status status = Status.ACTIVE;

    public int getParticipants() {
        return participants.size();
    }

    public Status getStatus() {
        return status;
    }

    public boolean isActive() {
        return status.equals(Status.ACTIVE);
    }

    public void mustBeActive() throws IllegalStateException {
        if (!isActive()) {
            throw new IllegalStateException("Transaction not active");
        }
    }

    public void join(TransactionParticipant p) {
        if (!participants.contains(p)) {
            p.joinedTransaction(this);
            participants.add(p);
        }
    }

    public void prepare() throws TransactionFailedException {
        beginPrepare();
        try {
            for (TransactionParticipant p : participants) {
                p.prepare(this);
            }
            prepareSucceeded();

        } catch (Throwable t) {
            prepareFailed();
            throw new TransactionFailedException("Prepare failed", t);
        }
    }

    public void commit() {
        beginCommit();
        if (commitAllParticipants()) {
            commitSucceeded();
        } else {
            commitFailed();
        }
    }

    private boolean commitAllParticipants() {
        boolean allSucceeded = true;
        for (TransactionParticipant p : participants) {
            try {
                p.commit(this);
            } catch (Throwable t) {
                allSucceeded = false;
                logger.error("Commit failed for participant " + p, t);
            }
        }
        return allSucceeded;
    }

    // Status changes

    private void beginPrepare() {
        synchronized (lock) {
            if (!status.equals(Status.ACTIVE)) {
                throw new IllegalStateException("Transaction not active");
            }
            status = Status.PREPARING;
        }
    }

    private void prepareSucceeded() {
        status = Status.PREPARE_OK;
    }

    private void prepareFailed() {
        status = Status.PREPARE_FAILED;
    }

    private void beginCommit() {
        synchronized (lock) {
            if (!status.equals(Status.PREPARE_OK)) {
                throw new IllegalStateException("Not prepared");
            }
            status = Status.COMMITTING;
        }
    }

    private void commitSucceeded() {
        status = Status.COMMIT_OK;
    }

    private void commitFailed() {
        status = Status.COMMIT_FAILED;
    }

    public enum Status {
        ACTIVE,
        PREPARING, PREPARE_OK, PREPARE_FAILED,
        COMMITTING, COMMIT_OK, COMMIT_FAILED,
        ROLLING_BACK, ROLLED_BACK
    }
}
