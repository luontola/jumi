// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db;

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
