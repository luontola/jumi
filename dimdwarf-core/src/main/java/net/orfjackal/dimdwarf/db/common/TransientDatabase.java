// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db.common;

import net.orfjackal.dimdwarf.db.*;
import net.orfjackal.dimdwarf.tx.*;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @author Esko Luontola
 * @since 18.11.2008
 */
@ThreadSafe
public class TransientDatabase<H> implements Database<Blob, Blob>, TransactionParticipant {

    private final ConcurrentMap<String, TransientDatabaseTable<H>> openTables = new ConcurrentHashMap<String, TransientDatabaseTable<H>>();
    private final PersistedDatabase<H> db;
    private final H dbHandle;
    private final Transaction tx;
    private CommitHandle commitHandle;

    public TransientDatabase(PersistedDatabase<H> db, H dbHandle, Transaction tx) {
        this.db = db;
        this.dbHandle = dbHandle;
        this.tx = tx;
        tx.join(this);
    }

    public IsolationLevel getIsolationLevel() {
        return db.getIsolationLevel();
    }

    public Set<String> getTableNames() {
        return db.getTableNames();
    }

    public DatabaseTable<Blob, Blob> openTable(String name) {
        tx.mustBeActive();
        TransientDatabaseTable<H> table = getOpenedTable(name);
        if (table == null) {
            table = openNewTable(name);
        }
        return table;
    }

    private TransientDatabaseTable<H> getOpenedTable(String name) {
        return openTables.get(name);
    }

    private TransientDatabaseTable<H> openNewTable(String name) {
        PersistedDatabaseTable<H> backend = db.openTable(name);
        openTables.putIfAbsent(name, new TransientDatabaseTable<H>(backend, dbHandle, tx));
        return getOpenedTable(name);
    }

    public void prepare() throws Throwable {
        commitHandle = db.prepare(openTables.values(), dbHandle);
    }

    public void commit() {
        commitHandle.commit();
    }

    public void rollback() {
        if (commitHandle != null) {
            commitHandle.rollback();
        }
    }
}
