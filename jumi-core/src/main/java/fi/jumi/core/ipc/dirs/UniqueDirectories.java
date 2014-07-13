// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc.dirs;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.nio.file.*;

@NotThreadSafe
public class UniqueDirectories {

    public static Path createUniqueDir(Path parentDir, long sequence) throws IOException {
        Files.createDirectories(parentDir);
        while (true) {
            Path path = parentDir.resolve(Long.toString(sequence));
            try {
                return Files.createDirectory(path);
            } catch (FileAlreadyExistsException e) {
                sequence++;
            }
        }
    }
}
