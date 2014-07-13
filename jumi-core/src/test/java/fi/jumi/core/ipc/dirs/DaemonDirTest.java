// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc.dirs;

import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Path;

import static com.thewonggei.regexTester.hamcrest.RegexMatches.doesMatchRegex;
import static org.hamcrest.MatcherAssert.assertThat;

public class DaemonDirTest {

    @Rule
    public final TemporaryFolder tempDir = new TemporaryFolder();

    private Path baseDir;
    private DaemonDir daemonDir;

    @Before
    public void setup() {
        baseDir = tempDir.getRoot().toPath();
        daemonDir = new DaemonDir(baseDir);
    }

    @Test
    public void creates_command_directories() throws IOException {
        CommandDir dir = daemonDir.createCommandDir();

        assertThat("command dir", relativeToBaseDir(dir.getPath()), doesMatchRegex("commands/\\d+"));
        assertThat("request file", relativeToBaseDir(dir.getRequestPath()), doesMatchRegex("commands/\\d+/request"));
        assertThat("response file", relativeToBaseDir(dir.getResponsePath()), doesMatchRegex("commands/\\d+/response"));
    }

    private String relativeToBaseDir(Path path) {
        return normalized(baseDir.relativize(path));
    }

    private static String normalized(Path path) {
        return path.toString().replace('\\', '/');
    }
}
