// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db.inmemory;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Linked list which remembers multiple revisions of a value. The revisions are immutable, but revisions
 * older than the latest revision may be purged to free memory. Reading the latest revision is an O(1) operation
 * and reading older revisions is an O(N) operation.
 *
 * @author Esko Luontola
 * @since 19.8.2008
 */
@ThreadSafe
public class RevisionList<T> {

    public static final long NULL_REVISION = 0L;

    private final long revision;
    @Nullable private final T value;
    @Nullable private volatile RevisionList<T> previous;

    public RevisionList(long revision, @Nullable T value) {
        this(revision, value, null);
    }

    public RevisionList(long revision, @Nullable T value, @Nullable RevisionList<T> previous) {
        assert revision > NULL_REVISION;
        assert previous == null || previous.revision < revision;
        this.revision = revision;
        this.value = value;
        this.previous = previous;
    }

    @Nullable
    public T get(long revision) {
        for (RevisionList<T> node = this; node != null; node = node.previous) {
            if (node.revision <= revision) {
                return node.value;
            }
        }
        return null;
    }

    public void purgeRevisionsOlderThan(long revisionToKeep) {
        for (RevisionList<T> node = this; node != null; node = node.previous) {
            if (node.revision <= revisionToKeep) {
                node.previous = null;
                return;
            }
        }
    }

    public long getLatestRevision() {
        return revision;
    }

    public boolean isEmpty() {
        return value == null && !hasOldRevisions();
    }

    public boolean hasOldRevisions() {
        return previous != null;
    }

    public String toString() {
        String s = "#" + revision + ": " + value;
        if (previous != null) {
            s += ", " + previous.toString();
        }
        return s;
    }
}
