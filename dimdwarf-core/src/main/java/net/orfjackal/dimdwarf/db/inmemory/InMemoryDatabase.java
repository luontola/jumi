// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

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

    @TestOnly
    int getNumberOfKeys() {
        int sum = 0;
        for (InMemoryDatabaseTable table : tables.values()) {
            sum += table.getNumberOfKeys();
        }
        return sum;
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
