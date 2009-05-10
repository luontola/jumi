// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db;

import jdave.*;
import jdave.junit4.JDaveRunner;
import org.junit.runner.RunWith;

import java.math.BigInteger;

/**
 * @author Esko Luontola
 * @since 30.10.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class ConvertBigIntegerToBytesSpec extends Specification<Object> {

    private ConvertBigIntegerToBytes converter = new ConvertBigIntegerToBytes();

    private Blob[] bytesOfBigIntegersUpTo(int maxValue) {
        Blob[] bytes = new Blob[maxValue + 1];
        for (int i = 0; i <= maxValue; i++) {
            bytes[i] = converter.forth(BigInteger.valueOf(i));
        }
        return bytes;
    }

    @SuppressWarnings({"ConstantConditions"})
    private byte[] bytesOfValue(int value) {
        return converter.forth(BigInteger.valueOf(value)).getByteArray();
    }


    public class TheByteRepresentationOfBigIntegers {

        private static final int TEST_MAX = 300;    // large enough number for taking at least 2 bytes

        public void convertsBackToTheSameValue() {
            Blob[] bytes = bytesOfBigIntegersUpTo(TEST_MAX);
            for (int i = 0; i < bytes.length; i++) {
                BigInteger original = BigInteger.valueOf(i);
                BigInteger converted = converter.back(bytes[i]);
                //System.err.println(original + " -> " + converted);
                specify(converted, should.equal(original));
            }
        }

        public void sortsInNumericOrder() {
            Blob[] bytes = bytesOfBigIntegersUpTo(TEST_MAX);
            for (int i = 0; i < bytes.length - 1; i++) {
                Blob current = bytes[i];
                Blob next = bytes[i + 1];
                //System.err.println(current + " < " + next);
                boolean currentLessThanNext = current.compareTo(next) < 0;
                specify((Integer) i, currentLessThanNext);
            }
        }

        public void theFormatIsNumberOfSignificantBytesFollowedByTheActualBytes() {
            specify(bytesOfValue(0), should.containInOrder(new byte[]{0x00}));
            specify(bytesOfValue(1), should.containInOrder(new byte[]{0x01, 0x01}));
            specify(bytesOfValue(2), should.containInOrder(new byte[]{0x01, 0x02}));
            specify(bytesOfValue(127), should.containInOrder(new byte[]{0x01, 0x7F}));
            specify(bytesOfValue(128), should.containInOrder(new byte[]{0x01, (byte) 0x80}));
            specify(bytesOfValue(255), should.containInOrder(new byte[]{0x01, (byte) 0xFF}));
            specify(bytesOfValue(256), should.containInOrder(new byte[]{0x02, 0x01, 0x00}));
            specify(bytesOfValue(257), should.containInOrder(new byte[]{0x02, 0x01, 0x01}));
        }

        public void emptyBlobConvertsToNullBigInteger() {
            BigInteger converted = converter.back(Blob.EMPTY_BLOB);
            specify(converted, should.equal(null));
        }

        public void negativeValuesAreNotAllowed() {
            specify(new Block() {
                public void run() throws Throwable {
                    converter.forth(BigInteger.valueOf(-1));
                }
            }, should.raise(IllegalArgumentException.class, "Negative values are not allowed: -1"));
        }
    }
}
