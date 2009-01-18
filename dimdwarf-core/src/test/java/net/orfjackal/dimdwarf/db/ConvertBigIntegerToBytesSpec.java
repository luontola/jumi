/*
 * This file is part of Dimdwarf Application Server <http://dimdwarf.sourceforge.net/>
 *
 * Copyright (c) 2008-2009, Esko Luontola. All Rights Reserved.
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
