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

import net.orfjackal.dimdwarf.db.*;
import net.orfjackal.dimdwarf.db.common.*;
import net.orfjackal.dimdwarf.tx.Transaction;
import org.jetbrains.annotations.TestOnly;

import javax.annotation.concurrent.*;
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
@ThreadSafe
public class InMemoryDatabase implements PersistedDatabase<RevisionHandle> {

    private final ConcurrentMap<String, InMemoryDatabaseTable> tables = new ConcurrentHashMap<String, InMemoryDatabaseTable>();
    private final RevisionCounter revisionCounter = new RevisionCounter();

    public IsolationLevel getIsolationLevel() {
        return IsolationLevel.SNAPSHOT;
    }

    public Set<String> getTableNames() {
        return tables.keySet();
    }

    public PersistedDatabaseTable<RevisionHandle> openTable(String name) {
        InMemoryDatabaseTable table = getExistingTable(name);
        if (table == null) {
            table = createNewTable(name);
        }
        return table;
    }

    private InMemoryDatabaseTable getExistingTable(String name) {
        return tables.get(name);
    }

    private InMemoryDatabaseTable createNewTable(String name) {
        tables.putIfAbsent(name, new InMemoryDatabaseTable());
        return getExistingTable(name);
    }

    public Database<Blob, Blob> createNewConnection(Transaction tx) {
        RevisionHandle h = revisionCounter.openNewestRevision();
        return new TransientDatabase<RevisionHandle>(this, h, tx);
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
    long getCurrentRevision() {
        return revisionCounter.getNewestReadableRevision();
    }

    public CommitHandle prepare(Collection<TransientDatabaseTable<RevisionHandle>> updates, RevisionHandle handle) {
        return new DbCommitHandle(updates, handle);
    }


    @Immutable
    private class DbCommitHandle implements CommitHandle {

        private final Collection<TransientDatabaseTable<RevisionHandle>> updates;
        private final RevisionHandle handle;

        public DbCommitHandle(Collection<TransientDatabaseTable<RevisionHandle>> updates, RevisionHandle handle) {
            this.updates = Collections.unmodifiableCollection(new ArrayList<TransientDatabaseTable<RevisionHandle>>(updates));
            this.handle = handle;
            prepare();
        }

        private void prepare() {
            for (TransientDatabaseTable<RevisionHandle> update : updates) {
                update.prepare();
            }
        }

        public void commit() {
            try {
                handle.prepareWriteRevision();
                for (TransientDatabaseTable<RevisionHandle> update : updates) {
                    update.commit();
                }
            } finally {
                handle.commitWrites();
                purgeOldUnusedRevisions();
            }
        }

        public void rollback() {
            try {
                for (TransientDatabaseTable<RevisionHandle> update : updates) {
                    update.rollback();
                }
            } finally {
                handle.rollback();
                purgeOldUnusedRevisions();
            }
        }
    }
}
