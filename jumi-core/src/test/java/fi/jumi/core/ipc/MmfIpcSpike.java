// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class MmfIpcSpike {

    private static final int MESSAGES = 10 * 1000 * 1000;

    private static final byte CAN_WRITE = 0;
    private static final byte CAN_READ = 1;

    private static final int FILE_SIZE = 1024 * 1024;
    private static final int TAG_INDEX = 0;
    private static final int DATA_INDEX = 1;                // same cache line
//    private static final int DATA_INDEX = FILE_SIZE - 4;  // different cache line

    // ways of creating a memory barrier
    public static volatile boolean volatileField;
    public static final AtomicBoolean atomicBoolean = new AtomicBoolean();

    private static MappedByteBuffer openMemoryMappedFile() throws IOException {
        RandomAccessFile file = new RandomAccessFile("MmfIpcSpike.tmp", "rw");

        return file.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, FILE_SIZE);
    }

    private static void loopWait() throws Exception {
        atomicBoolean.lazySet(true);
//        atomicBoolean.set(true);
//        volatileField = true;
//        Thread.yield();
//        LockSupport.parkNanos(1);
//        Thread.sleep(1);

        // ## IPC latency results ##
        //
        // Intel Core 2 Quad Q6600 @ 3.0GHz (Kentsfield), Windows 7 64-bit
        //              1 cache line    2 cache lines
        // lazySet()
        // set()
        // volatile     99.9 ns         125.55 ns
        // yield()                      208.25 ns
        // parkNanos(1)                 695000.0 ns
        // sleep(1)                     704000.0 ns
        // none         deadlock        deadlock
        //
        // Intel Core i7 860 @ 2.8GHz (Lynnfield), Windows 7 64-bit, jdk1.7.0_17 64-bit
        //              1 cache line    2 cache lines
        // lazySet()    68.85 ns        91.9 ns
        // set()        47.7 ns         95.0 ns
        // volatile     47.65-78.6 ns   91.65 ns
        // yield()      74.1 ns         110.3 ns
        // parkNanos(1) 640900.0 ns     691700.0 ns
        // sleep(1)     711150.0 ns     655000.0 ns
        // none         deadlock        deadlock
    }

    public static class Writer {
        public static void main(String[] args) throws Exception {
            MappedByteBuffer map = openMemoryMappedFile();

            for (int value = 0; value < MESSAGES; value++) {

                while (map.get(TAG_INDEX) != CAN_WRITE) {
                    loopWait();
                }
                map.putInt(DATA_INDEX, value);
                map.put(TAG_INDEX, CAN_READ);
            }
        }
    }

    public static class Reader {
        public static void main(String[] args) throws Exception {
            MappedByteBuffer map = openMemoryMappedFile();

            long start = System.currentTimeMillis();
            for (int expected = 0; expected < MESSAGES; expected++) {

                while (map.get(TAG_INDEX) != CAN_READ) {
                    loopWait();
                }
                int value = map.getInt(DATA_INDEX);
                assertThat(value, is(expected));
                map.put(TAG_INDEX, CAN_WRITE);
            }
            long end = System.currentTimeMillis();

            long durationMs = end - start;
            double nsPerMessage = durationMs * 1000000.0 / MESSAGES;
            System.out.println("read " + MESSAGES + " messages in " + durationMs + " ms");
            System.out.println(nsPerMessage + " ns/roundtrip, " + (nsPerMessage / 2) + " ns IPC latency");
        }
    }
}
