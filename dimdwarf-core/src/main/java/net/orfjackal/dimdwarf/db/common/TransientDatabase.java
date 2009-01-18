/*
 * This file is part of Dimdwarf Application Server <http://dimdwarf.sourceforge.net/>
 *
 * Copyright (c) 2008-2009, Esko Luontola. All Rights Reserved.
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
