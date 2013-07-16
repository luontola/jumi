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

    private static final int MESSAGES = 1 * 1000 * 1000;

    private static final byte CAN_WRITE = 0;
    private static final byte CAN_READ = 1;

    private static final int FILE_SIZE = 1024 * 1024;
    private static final int TAG_INDEX = 0;
    private static final int DATA_INDEX = FILE_SIZE - 4;

    private static MappedByteBuffer openMemoryMappedFile() throws IOException {
        RandomAccessFile file = new RandomAccessFile("/tmp/MmfIpcSpike", "rw");

        return file.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, FILE_SIZE);
    }

    private static void loopWait() throws Exception {
//        Thread.sleep(1);
        Thread.yield();
//        LockSupport.parkNanos(1);
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
