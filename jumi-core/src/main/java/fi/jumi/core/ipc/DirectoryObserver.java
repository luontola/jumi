// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import static java.nio.file.StandardWatchEventKinds.*;

@NotThreadSafe
public class DirectoryObserver implements Runnable {

    private final Path directory;
    private final Listener listener;
    private final Set<Path> seenFiles = new HashSet<>();

    public DirectoryObserver(Path directory, Listener listener) {
        this.directory = directory;
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            WatchService watcher = watchDirectory(directory, ENTRY_CREATE);

            handleAllFiles();

            while (!Thread.interrupted()) {
                WatchKey key = watcher.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == OVERFLOW) {
                        handleAllFiles();
                    } else {
                        Path filename = (Path) event.context();
                        handleFileName(filename);
                    }
                }
                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Throwable t) {
            throw new RuntimeException("Error in watching directory " + directory, t);
        }
    }

    private void handleAllFiles() throws IOException {
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(directory)) { // TODO: Java 8, use Files.list
            for (Path path : paths) {
                handleFileName(path.getFileName());
            }
        }
    }

    private void handleFileName(Path filename) {
        if (seenFiles.contains(filename)) {
            return;
        }
        seenFiles.add(filename);
        listener.onFileNoticed(directory.resolve(filename));
    }

    protected WatchService watchDirectory(Path directory, WatchEvent.Kind<?>... events) throws IOException {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        directory.register(watchService, events);
        return watchService;
    }

    public interface Listener {

        void onFileNoticed(Path path);
    }
}
