// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.testutils;

import org.apache.commons.io.FileUtils;
import org.junit.*;

import java.io.*;
import java.util.UUID;

import static net.orfjackal.dimdwarf.testutils.Matchers.containsFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class SandboxTest {

    private final File sandboxDir = new File(getClass().getSimpleName() + "_" + UUID.randomUUID().toString() + ".tmp");
    private final Sandbox sandbox = new Sandbox(sandboxDir);

    @Before
    public void createParentDir() {
        assertTrue("failed to create " + sandboxDir, sandboxDir.mkdir());
    }

    @After
    public void deleteParentDir() throws IOException {
        FileUtils.forceDelete(sandboxDir);
    }

    @Test
    public void temporary_directories_are_created_in_the_sandbox() {
        File dir = sandbox.createTempDir();

        assertThat(sandboxDir, containsFile(dir));
    }

    @Test
    public void each_temporary_directory_has_a_unique_name() {
        File dir1 = sandbox.createTempDir();
        File dir2 = sandbox.createTempDir();

        assertThat(dir1, is(not(dir2)));
    }

    @Test
    public void the_temporary_directories_are_deleted_recursively_but_not_the_sandbox() throws IOException {
        File dir = sandbox.createTempDir();

        assertTrue(new File(dir, "file1.txt").createNewFile());
        assertTrue(new File(dir, "subdir").mkdir());
        assertTrue(new File(dir, "subdir/file2.txt").createNewFile());

        sandbox.deleteTempDir(dir);

        assertThat("temp dir should not exist", dir.exists(), is(false));
        assertThat("sandbox should exist", sandboxDir.exists(), is(true));
    }

    @Test
    public void will_not_delete_files_outside_the_sandbox() {
        try {
            sandbox.deleteTempDir(sandboxDir);
            fail("should have thrown an exception");

        } catch (IllegalArgumentException e) {
            assertTrue("dir should still have existed", sandboxDir.exists());
        }
    }
}
