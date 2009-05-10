// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db;

import static net.orfjackal.dimdwarf.util.ByteUtil.asByteArray;

import javax.annotation.concurrent.Immutable;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * @author Esko Luontola
 * @since 18.8.2008
 */
@Immutable
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
            int thisUnsigned = bytes[i] & 0xFF;
            int otherUnsigned = other.bytes[i] & 0xFF;
            int cmp = thisUnsigned - otherUnsigned;
            if (cmp != 0) {
                return cmp;
            }
        }
        return bytes.length - other.bytes.length;
    }

    public String toString() {
        byte[] truncatedBytes = Arrays.copyOf(bytes, Math.min(bytes.length, TO_STRING_SAFETY_LIMIT));
        String hexBytes = asHex(truncatedBytes);
        if (bytes.length > TO_STRING_SAFETY_LIMIT) {
            hexBytes += " ...";
        }
        String asText = new String(truncatedBytes, Charset.forName("US-ASCII"));
        return "Blob[length=" + length() + ",bytes=[" + hexBytes + "],text=\"" + asText + "\"]";
    }

    private static String asHex(byte[] bytes) {
        StringBuilder hexBytes = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(b & 0xFF).toUpperCase();
            while (hex.length() < 2) {
                hex = "0" + hex;
            }
            hexBytes.append(hex).append(' ');
        }
        return hexBytes.toString().trim();
    }
}
