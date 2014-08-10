// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.daemon;

import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.junit.rules.*;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class DirBasedStewardTest {

    @Rule
    public final TemporaryFolder tempDir = new TemporaryFolder();

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private static final String expectedName = "daemon-1.2.3.jar";
    private final byte[] expectedContent = new byte[]{1, 2, 3};
    private final StubDaemonJar stubDaemonJar = new StubDaemonJar(expectedName, expectedContent);

    private Path jumiHome;
    private DirBasedSteward steward;

    @Before
    public void setup() throws IOException {
        jumiHome = tempDir.newFolder("jumiHome").toPath();
        steward = new DirBasedSteward(stubDaemonJar);
    }


    // Daemon directory

    @Test
    public void creates_a_daemon_directory() {
        Path daemonDir = steward.createDaemonDir(jumiHome);

        assertThat("should be under $JUMI_HOME/daemons", daemonDir.getParent(), is(jumiHome.resolve("daemons")));
        assertThat("should be a directory", Files.isDirectory(daemonDir), is(true));
    }

    @Test
    public void concurrently_created_daemons_will_always_get_their_own_daemon_directories() {
        List<Path> daemonDirs = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            daemonDirs.add(steward.createDaemonDir(jumiHome));
        }

        Set<Path> uniqueDaemonDirs = new HashSet<>(daemonDirs);
        assertThat("unique daemon dirs", uniqueDaemonDirs.size(), is(daemonDirs.size()));
    }

    @Test
    public void throws_exception_if_cannot_create_daemon_directory() throws IOException {
        Path parentDir = steward.createDaemonDir(jumiHome).getParent();
        FileUtils.deleteDirectory(parentDir.toFile());

        Files.createFile(parentDir); // prevents it from creating the daemon directory

        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Unable to create daemon directory");
        thrown.expectCause(instanceOf(FileAlreadyExistsException.class));
        steward.createDaemonDir(jumiHome);
    }


    // Daemon JAR

    @Test
    public void copies_the_embedded_daemon_JAR_to_the_settings_dir() throws IOException {
        Path daemonJar = steward.getDaemonJar(jumiHome);

        assertThat(daemonJar.getFileName().toString(), is(expectedName));
        assertThat(FileUtils.readFileToByteArray(daemonJar.toFile()), is(expectedContent));
    }

    @Test
    public void does_not_copy_the_daemon_JAR_if_it_has_already_been_copied() throws IOException {
        FileTime lastModified1 = Files.getLastModifiedTime(steward.getDaemonJar(jumiHome));
        FileTime lastModified2 = Files.getLastModifiedTime(steward.getDaemonJar(jumiHome));

        assertThat(lastModified2, is(lastModified1));
    }

    @Test
    public void overwrites_an_existing_daemon_JAR_that_has_difference_file_size() throws IOException {
        overwriteWithFileOfSize(expectedContent.length + 1, steward.getDaemonJar(jumiHome));

        Path daemonJar = steward.getDaemonJar(jumiHome);

        assertThat(FileUtils.readFileToByteArray(daemonJar.toFile()), is(expectedContent));
    }

    private static void overwriteWithFileOfSize(int fileSize, Path path) throws IOException {
        Files.write(path, new byte[fileSize]);
    }


    private static class StubDaemonJar implements DaemonJar {
        private final String name;
        private final byte[] content;

        private StubDaemonJar(String name, byte[] content) {
            this.name = name;
            this.content = content;
        }

        @Override
        public String getDaemonJarName() {
            return name;
        }

        @Override
        public InputStream getDaemonJarAsStream() {
            return new ByteArrayInputStream(content);
        }
    }
}
