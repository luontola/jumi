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
import net.orfjackal.dimdwarf.tx.Transaction;
import org.jetbrains.annotations.TestOnly;

import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.util.concurrent.*;

/**
 * An in-memory database which uses multiversion concurrency control.
 * The isolation mode is snapshot isolation - it allows write skew, but prevents phantom reads.
 * <p/>
 * See:
 * <a href="http://en.wikipedia.org/wiki/Multiversion_concurrency_control">multiversion concurrency control</a>,
 * <a href="http://en.wikipedia.org/wiki/Snapshot_isolation">snapshot isolation</a>
 *
 * @author Esko Luontola
 * @since 18.8.2008
 */
@Singleton
@ThreadSafe
public class InMemoryDatabase implements DatabaseManager, PersistedDatabase {

    // TODO: this class smells too big/messy
    // Responsibilities:
    // - keeps track of uncommitted database connections (InMemoryDatabaseManager?)
    // - knows which database tables exist (InMemoryDatabase?)
    // - can create new database tables (InMemoryDatabase?)
    // - prepare and commit modifications (InMemoryDatabase?)

    private final ConcurrentMap<Transaction, TransientDatabase> openConnections = new ConcurrentHashMap<Transaction, TransientDatabase>();
    private final Object commitLock = new Object();

    private final ConcurrentMap<String, InMemoryDatabaseTable> tables = new ConcurrentHashMap<String, InMemoryDatabaseTable>();
    private final RevisionCounter revisionCounter = new RevisionCounter();

    public IsolationLevel getIsolationLevel() {
        return IsolationLevel.SNAPSHOT;
    }

    // Tables

    public Set<String> getTableNames() {
        return tables.keySet();
    }

    public PersistedDatabaseTable openTable(String name) {
        PersistedDatabaseTable table = getExistingTable(name);
        if (table == null) {
            table = createNewTable(name);
        }
        return table;
    }

    private PersistedDatabaseTable getExistingTable(String name) {
        return tables.get(name);
    }

    private PersistedDatabaseTable createNewTable(String name) {
        tables.putIfAbsent(name, new InMemoryDatabaseTable());
        return getExistingTable(name);
    }

    // Connections

    public Database<Blob, Blob> openConnection(Transaction tx) {
        TransientDatabase db = getExistingConnection(tx);
        if (db == null) {
            db = createNewConnection(tx);
        }
        return db;
    }

    private TransientDatabase getExistingConnection(Transaction tx) {
        return openConnections.get(tx);
    }

    private TransientDatabase createNewConnection(Transaction tx) {
        TransientDatabase con = new TransientDatabase(this, tx, revisionCounter.openNewestRevision());
        Object prev = openConnections.putIfAbsent(tx, con);
        assert prev == null : "Connection " + prev + " already exists in transaction " + tx;
        return con;
    }

    private void closeConnection(Transaction tx) {
        TransientDatabase removed = openConnections.remove(tx);
        assert removed != null : "No connection open in transaction " + tx;
        purgeOldUnusedRevisions();
    }

    private void purgeOldUnusedRevisions() {
        long revisionToKeep = getOldestRevisionInUse();
        for (InMemoryDatabaseTable table : tables.values()) {
            table.purgeRevisionsOlderThan(revisionToKeep);
        }
    }

    long getOldestRevisionInUse() {
        return revisionCounter.getOldestReadableRevision();
    }

    @TestOnly
    int getOpenConnections() {
        return openConnections.size();
    }

    @TestOnly
    long getCurrentRevision() {
        return revisionCounter.getNewestReadableRevision();
    }

    // Transactions

    public CommitHandle prepare(Collection<TransientDatabaseTable> updates, Transaction tx) {
        return new MyCommitHandle(updates, tx);
    }


    @ThreadSafe
    private class MyCommitHandle implements CommitHandle {

        private final Collection<TransientDatabaseTable> updates;
        private final Transaction tx;

        public MyCommitHandle(Collection<TransientDatabaseTable> updates, Transaction tx) {
            this.updates = Collections.unmodifiableCollection(new ArrayList<TransientDatabaseTable>(updates));
            this.tx = tx;
            prepare();
        }

        // TODO: move prepare/commit/rollback details to TransientDatabase?

        private void prepare() {
            for (TransientDatabaseTable update : updates) {
                update.prepare();
            }
        }

        public void commit(long writeRevision) {
            try {
                for (TransientDatabaseTable update : updates) {
                    update.commit(writeRevision);
                }
            } finally {
                closeConnection(tx);
            }
        }

        public void rollback() {
            try {
                for (TransientDatabaseTable update : updates) {
                    update.rollback();
                }
            } finally {
                closeConnection(tx);
            }
        }
    }
}
