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

/**
 * @author Esko Luontola
 * @since 20.11.2008
 */
public class RevisionHandle {

    private static final long UNDEFINED = RevisionCounter.FIRST_REVISION - 1;

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
