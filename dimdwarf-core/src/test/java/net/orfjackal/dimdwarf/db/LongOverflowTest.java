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

/**
 * @author Esko Luontola
 * @since 29.10.2008
 */
public class LongOverflowTest {

    // TODO: overflowing long on C2Q6600 would take over 178000 days (487 years), so maybe it would be good enough to use longs as entity IDs?
    // also: 2^63 nanoseconds == 292 years

    public static void main(String[] args) {
        final long SPEEDUP = Integer.MAX_VALUE / 10;

        // warmup
        for (int i = 0; i < 10; i++) {
            countTime(100000);
        }

        // measurement
        long limit = Long.MAX_VALUE / SPEEDUP;
        System.out.println("expect:\t" + limit);
        long millis = countTime(limit);

        // results
        long estimateSeconds = millis * SPEEDUP / 1000;
        long estimateDays = estimateSeconds / 3600 / 24;
        System.out.println("\nOverflowing long would take " + estimateDays + " days");
    }

    private static long countTime(long increments) {
        long start = System.currentTimeMillis();
        long x;
        for (x = 0L; x < increments; x++) {
        }
        long end = System.currentTimeMillis();
        System.out.println("\t" + x);
        return end - start;
    }
}
