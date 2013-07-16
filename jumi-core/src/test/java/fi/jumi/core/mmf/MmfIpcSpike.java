// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.mmf;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class MmfIpcSpike {

    private static final int MESSAGES = 10 * 1000 * 1000;

    private static final byte CAN_WRITE = 0;
    private static final byte CAN_READ = 1;

    private static final int FILE_SIZE = 1024 * 1024;
    private static final int TAG_INDEX = 0;
    private static final int DATA_INDEX = FILE_SIZE - 4;

    public static volatile boolean memoryBarrier;

    private static MappedByteBuffer openMemoryMappedFile() throws IOException {
        RandomAccessFile file = new RandomAccessFile("/tmp/MmfIpcSpike", "rw");

        return file.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, FILE_SIZE);
    }

    private static void loopWait() throws Exception {
        // Measured on Intel Core 2 Quad Q6600 (Kentsfield), 2.4GHz @ 3.0GHz
        memoryBarrier = true;       // 125.55 ns IPC latency (two cache lines), 99.9 ns IPC latency (one cache line)
//        Thread.yield();           // 208.25 ns IPC latency
//        LockSupport.parkNanos(1); // 695000.0 ns IPC latency
//        Thread.sleep(1);          // 704000.0 ns IPC latency
//        ;                         // deadlocks, maybe because of a compiler optimization?
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
