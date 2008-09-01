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

package net.orfjackal.dimdwarf.db;

import static net.orfjackal.dimdwarf.util.ByteUtil.asByteArray;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * This class is immutable.
 *
 * @author Esko Luontola
 * @since 18.8.2008
 */
public final class Blob implements Comparable<Blob> {

    public static final Blob EMPTY_BLOB = new Blob(new byte[0]);
    private static final int TO_STRING_SAFETY_LIMIT = 100;

    private final byte[] bytes;
    private volatile Integer hashCode;

    private Blob(byte[] bytes) {
        this.bytes = bytes;
        this.hashCode = null;
    }

    public static Blob fromBytes(byte[] bytes) {
        return new Blob(Arrays.copyOf(bytes, bytes.length));
    }

    public static Blob fromInputStream(InputStream in) throws IOException {
        return new Blob(asByteArray(in));
    }

    public static Blob fromByteBuffer(ByteBuffer buffer) {
        return new Blob(asByteArray(buffer));
    }

    public int length() {
        return bytes.length;
    }

    public ByteArrayInputStream getInputStream() {
        return new ByteArrayInputStream(bytes);
    }

    public ByteBuffer getByteBuffer() {
        return ByteBuffer.wrap(bytes).asReadOnlyBuffer();
    }

    public byte[] getByteArray() {
        return Arrays.copyOf(bytes, bytes.length);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Blob) {
            Blob other = (Blob) obj;
            return Arrays.equals(this.bytes, other.bytes);
        }
        return false;
    }

    public int hashCode() {
        if (hashCode == null) {
            synchronized (bytes) {
                hashCode = Arrays.hashCode(bytes);
            }
        }
        return hashCode;
    }

    public int compareTo(Blob other) {
        if (this == other) {
            return 0;
        }
        for (int i = 0; i < bytes.length && i < other.bytes.length; i++) {
            int cmp = bytes[i] - other.bytes[i];
            if (cmp != 0) {
                return cmp;
            }
        }
        return bytes.length - other.bytes.length;
    }

    public String toString() {
        byte[] truncatedBytes = Arrays.copyOf(bytes, Math.min(bytes.length, TO_STRING_SAFETY_LIMIT));
        return "Blob[length=" + length() + ",bytes=" + Arrays.toString(truncatedBytes) + "]";
    }
}
