// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.test.util;

import org.junit.Test;

import java.io.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class TestEnvironmentTest {

    @Test
    public void sandbox_directory_is_created() {
        File sandbox = TestEnvironment.getSandboxDir();

        assertThat("sandbox: " + sandbox,
                sandbox.isDirectory(), is(true));
    }

    @Test
    public void sandbox_contains_the_deployment_directory() {
        File sandbox = TestEnvironment.getSandboxDir();
        File deployment = TestEnvironment.getDeploymentDir();

        assertThat(sandbox.listFiles(), hasItemInArray(deployment));
    }

    @Test
    public void deployment_directory_contains_the_deployed_application() {
        File deployment = TestEnvironment.getDeploymentDir();

        assertThat(deployment.list(), hasItemInArray("LICENSE.txt"));
    }

    @Test
    public void temporary_directories_can_be_created_in_the_sandbox() {
        File sandbox = TestEnvironment.getSandboxDir();
        File temp = TestEnvironment.createTempDir();

        try {
            assertThat(sandbox.listFiles(), hasItemInArray(temp));

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

        assertThat(sandbox.listFiles(), not(hasItemInArray(temp)));
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    private static void createNestedFileHierarchyIn(File baseDir) throws IOException {
        File f1 = new File(baseDir, "sub-directory");
        File f2 = new File(f1, "file-in-sub-directory");
        f1.mkdir();
        f2.createNewFile();
    }
}
