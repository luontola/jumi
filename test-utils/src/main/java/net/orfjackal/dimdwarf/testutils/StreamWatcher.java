// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.testutils;

import java.io.Reader;
import java.util.Scanner;
import java.util.concurrent.*;

public class StreamWatcher {

    private static final Object END_OF_STREAM = new Object();

    private final BlockingQueue<Object> lines = new LinkedBlockingQueue<Object>();

    public StreamWatcher(Reader source) {
        copyInBackground(source);
    }

    private void copyInBackground(final Reader source) {
        Thread t = new Thread(new Runnable() {
            public void run() {
                Scanner in = new Scanner(source);
                while (in.hasNextLine()) {
                    lines.add(in.nextLine());
                }
                lines.add(END_OF_STREAM);
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public void waitForLineContaining(String expected, int timeout, TimeUnit unit) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + unit.toMillis(timeout);
        StringBuilder seen = new StringBuilder();
        Object obj;
        do {
            obj = lines.poll(endTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
            if (obj instanceof String) {
                String line = (String) obj;
                seen.append(line).append('\n');
                if (line.contains(expected)) {
                    return;
                }
            }
        } while (System.currentTimeMillis() < endTime && obj != END_OF_STREAM);
        throw new AssertionError("Expected to find: " + expected + "\nbut was:\n" + seen);
    }
}
