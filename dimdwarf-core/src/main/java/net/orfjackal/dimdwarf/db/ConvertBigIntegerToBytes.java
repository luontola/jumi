// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db;

import javax.annotation.concurrent.Immutable;
import java.math.BigInteger;

/**
 * @author Esko Luontola
 * @since 12.9.2008
 */
@Immutable
public class ConvertBigIntegerToBytes implements Converter<BigInteger, Blob> {

    public BigInteger back(Blob value) {
        if (value == null || value.length() == 0) {
            return null;
        }
        return new BigInteger(1, unpack(value.getByteArray()));
    }

    public Blob forth(BigInteger value) {
        if (value == null) {
            return null;
        }
        if (value.signum() < 0) {
            throw new IllegalArgumentException("Negative values are not allowed: " + value);
        }
        return Blob.fromBytes(pack(value.toByteArray()));
    }

    private static byte[] unpack(byte[] packed) {
        byte[] bytes = new byte[packed.length - 1];
        assert bytes.length == packed[0];
        System.arraycopy(packed, 1, bytes, 0, bytes.length);
        return bytes;
    }

    private static byte[] pack(byte[] bytes) {
        int leadingNullBytes = getLeadingNullBytes(bytes);
        int significantBytes = bytes.length - leadingNullBytes;
        byte[] packed = new byte[significantBytes + 1];
        packed[0] = (byte) significantBytes;
        System.arraycopy(bytes, leadingNullBytes, packed, 1, significantBytes);
        return packed;
    }

    private static int getLeadingNullBytes(byte[] bytes) {
        int leadingNullBytes = 0;
        for (int i = 0; i < bytes.length && bytes[i] == 0x00; i++) {
            leadingNullBytes++;
        }
        return leadingNullBytes;
    }
}
