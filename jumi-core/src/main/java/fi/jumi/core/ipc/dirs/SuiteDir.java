// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc.dirs;

import javax.annotation.concurrent.Immutable;
import java.nio.file.Path;

@Immutable
public class SuiteDir {

    private final Path path;

    public SuiteDir(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    public Path getSuiteResultsPath() {
        return path.resolve("suite");
    }
}
