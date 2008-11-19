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

import javax.annotation.concurrent.ThreadSafe;

/**
 * The latest revision of the list is immutable (i.e. the only possible modification is that old revisions are purged).
 *
 * @author Esko Luontola
 * @since 19.8.2008
 */
@ThreadSafe
public class RevisionList<T> {

    private final long revision;
    private final T value;
    private volatile RevisionList<T> previous;

    public RevisionList(long revision, T value) {
        this(revision, value, null);
    }

    public RevisionList(long revision, T value, RevisionList<T> previous) {
        this.revision = revision;
        this.value = value;
        this.previous = previous;
    }

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

    public long latestRevision() {
        return revision;
    }

    public boolean isEmpty() {
        return value == null && !hasOldRevisions();
    }

    public boolean hasOldRevisions() {
        return previous != null;
    }
}
