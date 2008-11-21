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

package net.orfjackal.dimdwarf.db.inmemory;

import com.google.inject.Singleton;
import net.orfjackal.dimdwarf.db.*;
import net.orfjackal.dimdwarf.tx.*;
import org.jetbrains.annotations.TestOnly;

import javax.annotation.concurrent.*;
import java.util.concurrent.*;

/**
 * @author Esko Luontola
 * @since 21.11.2008
 */
@Singleton
@ThreadSafe
public class InMemoryDatabaseManager implements DatabaseManager {

    private final InMemoryDatabase db = new InMemoryDatabase();
    private final ConcurrentMap<Transaction, Database<Blob, Blob>> openConnections = new ConcurrentHashMap<Transaction, Database<Blob, Blob>>();

    public Database<Blob, Blob> openConnection(Transaction tx) {
        Database<Blob, Blob> connection = getExistingConnection(tx);
        if (connection == null) {
            connection = createNewConnection(tx);
        }
        return connection;
    }

    private Database<Blob, Blob> getExistingConnection(Transaction tx) {
        return openConnections.get(tx);
    }

    private Database<Blob, Blob> createNewConnection(Transaction tx) {
        Database<Blob, Blob> con = db.createNewConnection(tx);
        Object prev = openConnections.putIfAbsent(tx, con);
        assert prev == null : "Connection " + prev + " already exists in transaction " + tx;
        tx.join(new ConnectionCloser(tx));
        return con;
    }

    private void closeConnection(Transaction tx) {
        Object removed = openConnections.remove(tx);
        assert removed != null : "No connection open in transaction " + tx;
    }

    @TestOnly
    int getOpenConnections() {
        return openConnections.size();
    }

    @TestOnly
    long getOldestRevisionInUse() {
        return db.getOldestRevisionInUse();
    }

    @TestOnly
    long getCurrentRevision() {
        return db.getCurrentRevision();
    }


    @Immutable
    private class ConnectionCloser implements TransactionParticipant {

        private final Transaction tx;

        public ConnectionCloser(Transaction tx) {
            this.tx = tx;
        }

        public void prepare() throws Throwable {
        }

        public void commit() {
            closeConnection(tx);
        }

        public void rollback() {
            closeConnection(tx);
        }
    }
}
