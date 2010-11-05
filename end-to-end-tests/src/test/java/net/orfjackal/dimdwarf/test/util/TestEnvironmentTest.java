// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.test.util;

import org.junit.Test;

import java.io.*;

import static net.orfjackal.dimdwarf.testutils.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

public class TestEnvironmentTest {

    @Test
    public void sandbox_directory_is_created() {
        File sandbox = TestEnvironment.getSandboxDir();

        assertThat(sandbox, isDirectory());
    }

    @Test
    public void sandbox_contains_the_server_home_directory() {
        File sandbox = TestEnvironment.getSandboxDir();
        File serverHome = TestEnvironment.getServerHomeDir();

        assertThat(sandbox, containsFile(serverHome));
    }

    @Test
    public void server_home_directory_contains_the_server_distribution() {
        File serverHome = TestEnvironment.getServerHomeDir();

        assertThat(serverHome, containsFile("LICENSE.txt"));
    }

    @Test
    public void temporary_directories_can_be_created_in_the_sandbox() {
        File sandbox = TestEnvironment.getSandboxDir();
        File temp = TestEnvironment.createTempDir();

        try {
            assertThat(sandbox, containsFile(temp));

        } finally {
            TestEnvironment.deleteTempDir(temp);
        }
    }

    @Test
    public void temporary_directories_are_removed_recursively() throws IOException {
        File sandbox = TestEnvironment.getSandboxDir();
        File temp = TestEnvironment.createTempDir();
        createNestedFileHierarchyIn(temp);

        TestEnvironment.deleteTempDir(temp);

        assertThat(sandbox, not(containsFile(temp)));
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    private static void createNestedFileHierarchyIn(File baseDir) throws IOException {
        File f1 = new File(baseDir, "sub-directory");
        File f2 = new File(f1, "file-in-sub-directory");
        f1.mkdir();
        f2.createNewFile();
    }

    @Test
    public void application_base_jar_with_classes_for_end_to_end_tests_is_provided() {
        File applicationBaseJar = TestEnvironment.getApplicationBaseJar();

        assertThat(applicationBaseJar, isJarFile());
    }
}
