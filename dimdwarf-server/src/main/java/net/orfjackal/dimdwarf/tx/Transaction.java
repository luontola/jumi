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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Esko Luontola
 * @since 15.8.2008
 */
public class Transaction {

    private final List<TransactionParticipant> participants = new ArrayList<TransactionParticipant>();
    private volatile Status status = Status.ACTIVE;
    private final Object lock = new Object();

    public int getParticipants() {
        return participants.size();
    }

    public Status getStatus() {
        return status;
    }

    public void mustBeActive() {
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

    public void commit() {
        throw new IllegalStateException("Not prepared");
    }

    public enum Status {
        ACTIVE, PREPARING, PREPARE_OK, PREPARE_FAILED, COMMITTING, COMMIT_OK, COMMIT_FAILED, ROLLED_BACK
    }
}
