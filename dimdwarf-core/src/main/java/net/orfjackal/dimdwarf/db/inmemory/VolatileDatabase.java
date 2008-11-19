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
import net.orfjackal.dimdwarf.tx.*;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @author Esko Luontola
 * @since 18.11.2008
 */
@ThreadSafe
public class VolatileDatabase implements Database<Blob, Blob>, TransactionParticipant {

    private final ConcurrentMap<String, VolatileDatabaseTable> openTables = new ConcurrentHashMap<String, VolatileDatabaseTable>();
    private final long visibleRevision;
    private final Transaction tx;
    private final PersistedDatabase db;
    private CommitHandle commitHandle;

    public VolatileDatabase(PersistedDatabase db, long visibleRevision, Transaction tx) {
        this.db = db;
        this.visibleRevision = visibleRevision;
        this.tx = tx;
        tx.join(this);
    }

    public long getVisibleRevision() {
        return visibleRevision;
    }

    public IsolationLevel getIsolationLevel() {
        return db.getIsolationLevel();
    }

    public Set<String> getTableNames() {
        return db.getTableNames();
    }

    public DatabaseTable<Blob, Blob> openTable(String name) {
        tx.mustBeActive();
        VolatileDatabaseTable table = getCachedTable(name);
        if (table == null) {
            table = cacheNewTable(name);
        }
        return table;
    }

    private VolatileDatabaseTable getCachedTable(String name) {
        return openTables.get(name);
    }

    private VolatileDatabaseTable cacheNewTable(String name) {
        PersistedDatabaseTable backend = db.openTable(name);
        openTables.putIfAbsent(name, new VolatileDatabaseTable(backend, visibleRevision, tx));
        return getCachedTable(name);
    }

    public void prepare() throws Throwable {
        commitHandle = db.prepare(openTables.values(), tx);
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
