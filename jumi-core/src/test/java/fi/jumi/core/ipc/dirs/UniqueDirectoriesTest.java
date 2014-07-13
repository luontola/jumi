// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc.dirs;

import fi.jumi.core.Timeouts;
import org.junit.*;
import org.junit.rules.*;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;

import static fi.jumi.core.util.PredicateMatchers.satisfies;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assume.assumeTrue;

public class UniqueDirectoriesTest {

    @Rule
    public final TemporaryFolder tempDir = new TemporaryFolder();

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Rule
    public final Timeout timeout = Timeouts.forUnitTest();

    private Path baseDir;

    @Before
    public void setup() {
        baseDir = tempDir.getRoot().toPath();
    }

    @Test
    public void creates_directories_under_the_parent_dir() throws IOException {
        Path dir = UniqueDirectories.createUniqueDir(baseDir, 123);

        assertThat("creates the directory", dir, satisfies(p -> Files.isDirectory(p)));
        assertThat("is child of parent dir", dir.getParent(), is(baseDir));
        assertThat("name is based on the sequence number", dir.getFileName(), is(Paths.get("123")));
    }

    @Test
    public void creates_parent_dirs_if_necessary() throws IOException {
        Path parentDir = baseDir.resolve("does/not/exist");

        UniqueDirectories.createUniqueDir(parentDir, 123);

        assertThat("creates the parent dir", parentDir, satisfies(p -> Files.isDirectory(p)));
    }

    @Test
    public void will_retry_with_another_name_in_sequence_when_directory_already_exists() throws IOException {
        Files.createDirectory(baseDir.resolve("123"));

        Path dir = UniqueDirectories.createUniqueDir(baseDir, 123);

        assertThat("creates the directory", dir, satisfies(p -> Files.isDirectory(p)));
        assertThat("name is next in sequence", dir.getFileName(), is(Paths.get("124")));
    }

    @Test
    public void will_fail_if_unable_to_create_the_directory_for_permission_or_other_reasons() throws IOException {
        assumeTrue("this test works only on POSIX compatible file systems",
                Files.getFileAttributeView(baseDir, PosixFileAttributeView.class) != null);

        Set<PosixFilePermission> noPermissions = new HashSet<>();
        Files.setPosixFilePermissions(baseDir, noPermissions);
        assertThat("the file system did not let us change file permissions",
                Files.getPosixFilePermissions(baseDir), is(noPermissions));

        thrown.expect(AccessDeniedException.class);
        UniqueDirectories.createUniqueDir(baseDir, 123);
    }

    @Test
    public void will_fail_if_unable_to_create_the_parent_directory() throws IOException {
        Path parent = Files.createFile(baseDir.resolve("not-a-directory"));

        thrown.expect(FileAlreadyExistsException.class);
        UniqueDirectories.createUniqueDir(parent, 123);
    }
}
