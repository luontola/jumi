// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db.common;

import net.orfjackal.dimdwarf.db.*;
import net.orfjackal.dimdwarf.tx.Transaction;

import javax.annotation.*;
import javax.annotation.concurrent.ThreadSafe;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

@ThreadSafe
public class TransientDatabaseTable<H> implements DatabaseTable<Blob, Blob> {

    private final SortedMap<Blob, Blob> updates = new ConcurrentSkipListMap<Blob, Blob>();
    private final PersistedDatabaseTable<H> dbTable;
    private final H dbHandle;
    private final Transaction tx;
    private CommitHandle commitHandle;

    public TransientDatabaseTable(PersistedDatabaseTable<H> dbTable, H dbHandle, Transaction tx) {
        this.dbTable = dbTable;
        this.dbHandle = dbHandle;
        this.tx = tx;
    }

    public boolean exists(Blob key) {
        return read(key).length() > 0;
    }

    @Nonnull
    public Blob read(Blob key) {
        tx.mustBeActive();
        Blob blob = updates.get(key);
        if (blob == null) {
            blob = dbTable.get(key, dbHandle);
        }
        if (blob == null) {
            blob = Blob.EMPTY_BLOB;
        }
        return blob;
    }

    public void update(Blob key, Blob value) {
        tx.mustBeActive();
        updates.put(key, value);
    }

    public void delete(Blob key) {
        tx.mustBeActive();
        updates.put(key, Blob.EMPTY_BLOB);
    }

    public Blob firstKey() {
        tx.mustBeActive();
        Blob key1 = SortedMapUtil.firstKey(updates);
        Blob key2 = dbTable.firstKey(dbHandle);
        Blob first = min(key1, key2);
        if (first != null && !exists(first)) {
            first = nextKeyAfter(first);
        }
        return first;
    }

    public Blob nextKeyAfter(Blob currentKey) {
        tx.mustBeActive();
        Blob next = currentKey;
        do {
            Blob key1 = SortedMapUtil.nextKeyAfter(next, updates);
            Blob key2 = dbTable.nextKeyAfter(next, dbHandle);
            next = min(key1, key2);
        } while (next != null && !exists(next));
        return next;
    }

    @Nullable
    private static <T extends Comparable<T>> T min(@Nullable T a, @Nullable T b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        if (a.compareTo(b) < 0) {
            return a;
        } else {
            return b;
        }
    }

    public void prepare() {
        commitHandle = dbTable.prepare(updates, dbHandle);
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
