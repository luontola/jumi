// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.*;

public class FakeWatchService implements WatchService {

    private final BlockingQueue<WatchKey> events = new LinkedBlockingQueue<>();

    public void publish(WatchKey event) {
        events.add(event);
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public WatchKey poll() {
        return events.poll();
    }

    @Override
    public WatchKey poll(long timeout, TimeUnit unit) throws InterruptedException {
        return events.poll(timeout, unit);
    }

    @Override
    public WatchKey take() throws InterruptedException {
        return events.take();
    }
}
