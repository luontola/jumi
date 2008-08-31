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

package net.orfjackal.dimdwarf.tx;

/**
 * @author Esko Luontola
 * @since 17.8.2008
 */
public interface TransactionParticipant {

    /**
     * Called by the transaction coordinator when this participant joins a transaction.
     */
    void joinedTransaction(Transaction tx);

    /**
     * Called by the transaction coordinator to signify that this participant should prepare for commit.
     * If the participant is unable to prepare (for example because of a transaction conflict), it may
     * throw an exception, in which case the transaction is rolled back.
     */
    void prepare(Transaction tx) throws Throwable;

    /**
     * Called by the transaction coordinator to signify that this participant commit. The participant is
     * not allowed to fail the commit, but if it anyways throws an exception, the commit of other participants
     * is not interrupted and the system will possibly end up in an inconsistent state.
     */
    void commit(Transaction tx);

    /**
     * Called by the transaction coordinator to signify that this participant rollback. The participant is
     * not allowed to fail the rollback, but if anyways throws an exception, the rollback of other participants
     * is not interrupted and the system will possibly end up in an inconsistent state.
     */
    void rollback(Transaction tx);
}
