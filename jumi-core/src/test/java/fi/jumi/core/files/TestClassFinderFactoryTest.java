// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.files;

import fi.jumi.core.config.SuiteConfigurationBuilder;
import org.junit.*;
import org.junit.rules.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TestClassFinderFactoryTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Rule
    public final TemporaryFolder tempDir = new TemporaryFolder();

    private final ClassLoader cl = getClass().getClassLoader();

    @Test
    public void when_testClasses_is_set_creates_an_enumerated_finder() {
        assertCreates(new SuiteConfigurationBuilder().testClasses("Foo", "Bar"),
                new EnumeratedTestClassFinder(Arrays.asList("Foo", "Bar"), cl));
    }

    @Ignore // TODO: update
    @Test
    public void when_testFileMatcher_is_set_creates_an_enumerated_finder() {
        Path baseDir = Paths.get(".").toAbsolutePath();
        PathMatcher matcher = baseDir.getFileSystem().getPathMatcher("glob:the pattern");

        assertCreates(new SuiteConfigurationBuilder()
                .includedTestsPattern("glob:the pattern")
                .addToClassPath(baseDir),
                compositeOf(new FileNamePatternTestClassFinder(matcher, baseDir, cl)));
    }

    @Test
    public void testClasses_takes_precedence_over_testFileMatcher() {
        assertCreates(new SuiteConfigurationBuilder().testClasses("Foo", "Bar").includedTestsPattern("glob:the pattern"),
                new EnumeratedTestClassFinder(Arrays.asList("Foo", "Bar"), cl));
    }

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

        List<Path> classesDirectories = TestClassFinderFactory.getClassesDirectories(suite.freeze());
        assertThat(classesDirectories, contains(folder1, folder2));
    }


    private void assertCreates(SuiteConfigurationBuilder suite, TestClassFinder expected) {
        assertThat(TestClassFinderFactory.create(suite.freeze(), cl), is(expected));
    }

    private static CompositeTestClassFinder compositeOf(TestClassFinder... finders) {
        return new CompositeTestClassFinder(Arrays.asList(finders));
    }
}
