// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core;

import fi.jumi.core.config.SuiteConfigurationBuilder;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.*;

public class TestRunCoordinatorTest {

    @Rule
    public final TemporaryFolder tempDir = new TemporaryFolder();

    @Test
    public void looks_for_tests_from_directories_on_classpath() throws IOException {
        Path libraryJar = tempDir.newFile("library.jar").toPath();
        Path folder1 = tempDir.newFolder("folder1").toPath();
        Path folder2 = tempDir.newFolder("folder2").toPath();

        SuiteConfigurationBuilder suite = new SuiteConfigurationBuilder()
                .includedTestsPattern("glob:the pattern")
                .addToClassPath(libraryJar)
                .addToClassPath(folder1)
                .addToClassPath(folder2);

        List<Path> classesDirectories = TestRunCoordinator.getClassDirectories(suite.freeze());
        assertThat(classesDirectories, contains(folder1, folder2));
    }

    @Test
    public void runs_the_shutdown_hook_when_told_to_shutdown() {
        Runnable shutdownHook = mock(Runnable.class);
        TestRunCoordinator coordinator = new TestRunCoordinator(null, null, shutdownHook, null);

        coordinator.shutdown();

        verify(shutdownHook).run();
    }
}
