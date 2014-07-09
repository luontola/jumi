// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import javax.annotation.concurrent.GuardedBy;
import java.nio.file.*;
import java.util.*;

public class FakeWatchKey implements WatchKey {

    @GuardedBy("this")
    private ArrayList<WatchEvent<?>> events = new ArrayList<>();

    public synchronized FakeWatchKey addEvent(WatchEvent.Kind<Object> kind) {
        events.add(new WatchEvent<Object>() {
            @Override
            public Kind<Object> kind() {
                return kind;
            }

            @Override
            public int count() {
                return 1;
            }

            @Override
            public Object context() {
                return null;
            }
        });
        return this;
    }

    public synchronized FakeWatchKey addEvent(WatchEvent.Kind<Path> kind, Path path) {
        events.add(new WatchEvent<Path>() {
            @Override
            public Kind<Path> kind() {
                return kind;
            }

            @Override
            public int count() {
                return 1;
            }

            @Override
            public Path context() {
                return path;
            }
        });
        return this;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public synchronized List<WatchEvent<?>> pollEvents() {
        try {
            return events;
        } finally {
            events = new ArrayList<>();
        }
    }

    @Override
    public boolean reset() {
        return true;
    }

    @Override
    public void cancel() {
    }

    @Override
    public Watchable watchable() {
        return null;
    }
}
