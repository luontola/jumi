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

package net.orfjackal.dimdwarf.db;

import net.orfjackal.dimdwarf.tx.Transaction;
import net.orfjackal.dimdwarf.tx.TransactionParticipant;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Esko Luontola
 * @since 18.8.2008
 */
public class InMemoryDatabase {

    private final Map<Blob, Blob> values = new ConcurrentHashMap<Blob, Blob>();

    public Database openConnection(Transaction tx) {
        TransactionalDatabase db = new TransactionalDatabase();
        tx.join(db);
        return db;
    }

    private class TransactionalDatabase implements Database, TransactionParticipant {

        private Transaction tx;
        private final Map<Blob, Blob> updates = new ConcurrentHashMap<Blob, Blob>();

        public void joinedTransaction(Transaction tx) {
            if (this.tx != null) {
                throw new IllegalStateException();
            }
            this.tx = tx;
        }

        public void prepare(Transaction tx) throws Throwable {
        }

        public void commit(Transaction tx) {
            for (Map.Entry<Blob, Blob> entry : updates.entrySet()) {
                values.put(entry.getKey(), entry.getValue());
            }
        }

        public void rollback(Transaction tx) {
        }

        public Blob read(Blob key) {
            Blob blob = updates.get(key);
            if (blob == null) {
                blob = values.get(key);
            }
            return blob;
        }

        public void update(Blob key, Blob value) {
            updates.put(key, value);
        }

        public void delete(Blob key) {
            updates.remove(key);
        }
    }
}
