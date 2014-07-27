// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc.dirs;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.nio.file.Path;

@Immutable
public final class DaemonDir {

    private final Path baseDir;

    public DaemonDir(Path baseDir) {
        this.baseDir = baseDir;
    }

    public Path getCommandsDir() {
        return baseDir.resolve("commands");
    }

    public CommandDir createCommandDir() throws IOException {
        return new CommandDir(createUniqueDirUnder(getCommandsDir()));
    }

    private Path getSuitesDir() {
        return baseDir.resolve("suites");
    }

    public SuiteDir createSuiteDir() throws IOException {
        return new SuiteDir(createUniqueDirUnder(getSuitesDir()));
    }

    private Path createUniqueDirUnder(Path dir) throws IOException {
        return UniqueDirectories.createUniqueDir(dir, System.currentTimeMillis());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + baseDir + ")";
    }
}
