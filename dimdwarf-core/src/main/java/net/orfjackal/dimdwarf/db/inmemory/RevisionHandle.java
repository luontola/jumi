// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db.inmemory;

public class RevisionHandle {

    private static final long UNDEFINED = RevisionList.NULL_REVISION - 1;

    private final RevisionCounter controller;
    private final long readRevision;
    private long writeRevision = UNDEFINED;

    public RevisionHandle(long readRevision, RevisionCounter controller) {
        this.readRevision = readRevision;
        this.controller = controller;
    }

    public long getReadRevision() {
        return readRevision;
    }

    public long getWriteRevision() {
        if (writeRevision == UNDEFINED) {
            throw new IllegalStateException("Not prepared");
        }
        return writeRevision;
    }

    public boolean isWriteRevisionPrepared() {
        return writeRevision != UNDEFINED;
    }

    public void prepareWriteRevision() {
        assert !isWriteRevisionPrepared();
        writeRevision = controller.nextWriteRevision();
    }

    public void commitWrites() {
        controller.commitWrites(this);
    }

    public void rollback() {
        controller.rollback(this);
    }

    public String toString() {
        return getClass().getSimpleName() + "[read=" + readRevision + ",write=" + writeRevision + "]";
    }
}
