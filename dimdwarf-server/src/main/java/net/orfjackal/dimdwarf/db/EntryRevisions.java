/*
 * Copyright (c) 2008, Esko Luontola. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
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

package net.orfjackal.dimdwarf.db;

import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Esko Luontola
 * @since 18.8.2008
 */
public class EntryRevisions {

    private SortedMap<Integer, Blob> revs = new TreeMap<Integer, Blob>();

    public Blob read(int revision) {
        Blob value = null;
        for (Map.Entry<Integer, Blob> e : revs.entrySet()) {
            if (e.getKey() <= revision) {
                value = e.getValue();
            }
        }
        return value;
    }

    public void write(int revision, Blob value) {
        revs.put(revision, value);
    }

    public void checkNotModifiedAfter(int revision) {
        if (revs.size() > 0) {
            int lastWrite = revs.lastKey();
            if (lastWrite > revision) {
                throw new ConcurrentModificationException("Already modified in revision " + lastWrite);
            }
        }
    }
}
