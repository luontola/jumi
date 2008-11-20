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
import net.orfjackal.dimdwarf.tx.Transaction;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Esko Luontola
 * @since 18.11.2008
 */
@ThreadSafe
public class TransientDatabaseTable implements DatabaseTable<Blob, Blob> {

    private final Map<Blob, Blob> updates = new ConcurrentHashMap<Blob, Blob>();
    private final PersistedDatabaseTable table;
    private final long readRevision;
    private final Transaction tx;
    private RevisionCommitHandle commitHandle;

    public TransientDatabaseTable(PersistedDatabaseTable table, long readRevision, Transaction tx) {
        this.table = table;
        this.readRevision = readRevision;
        this.tx = tx;
    }

    public Blob read(Blob key) {
        tx.mustBeActive();
        Blob blob = updates.get(key);
        if (blob == null) {
            blob = table.get(key, readRevision);
        }
        if (blob == null) {
            blob = net.orfjackal.dimdwarf.db.Blob.EMPTY_BLOB;
        }
        return blob;
    }

    public void update(Blob key, Blob value) {
        tx.mustBeActive();
        updates.put(key, value);
    }

    public void delete(Blob key) {
        tx.mustBeActive();
        updates.put(key, net.orfjackal.dimdwarf.db.Blob.EMPTY_BLOB);
    }

    // TODO: 'firstKey' and 'nextKeyAfter' do not see keys which were created during this transaction

    public Blob firstKey() {
        tx.mustBeActive();
        return table.firstKey();
    }

    public Blob nextKeyAfter(Blob currentKey) {
        tx.mustBeActive();
        return table.nextKeyAfter(currentKey);
    }

    public void prepare() {
        commitHandle = table.prepare(updates, readRevision);
    }

    public void commit(long writeRevision) {
        commitHandle.commit(writeRevision);
    }

    public void rollback() {
        if (commitHandle != null) {
            commitHandle.rollback();
        }
    }
}
