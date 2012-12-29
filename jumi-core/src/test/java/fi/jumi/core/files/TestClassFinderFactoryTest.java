// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.files;

import fi.jumi.core.config.SuiteConfigurationBuilder;
import org.junit.*;
import org.junit.rules.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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

    @Test
    public void when_testFileMatcher_is_set_creates_an_enumerated_finder() {
        assertCreates(new SuiteConfigurationBuilder().includedTestsPattern("the pattern").addToClassPath(Paths.get(".")),
                compositeOf(new FileNamePatternTestClassFinder("the pattern", Paths.get(".").toAbsolutePath(), cl)));
    }

    @Test
    public void testClasses_takes_precedence_over_testFileMatcher() {
        assertCreates(new SuiteConfigurationBuilder().testClasses("Foo", "Bar").includedTestsPattern("the pattern"),
                new EnumeratedTestClassFinder(Arrays.asList("Foo", "Bar"), cl));
    }

    @Test
    public void testClasses_or_testFileMatcher_is_required() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("testClasses and includedTestsPattern were both empty");
        TestClassFinderFactory.create(new SuiteConfigurationBuilder().includedTestsPattern("").freeze(), cl);
    }

    @Test
    public void looks_for_tests_from_directories_on_classpath() throws IOException {
        Path libraryJar = tempDir.newFile("library.jar").toPath();
        Path folder1 = tempDir.newFolder("folder1").toPath();
        Path folder2 = tempDir.newFolder("folder2").toPath();

        SuiteConfigurationBuilder suite = new SuiteConfigurationBuilder()
                .includedTestsPattern("the pattern")
                .addToClassPath(libraryJar)
                .addToClassPath(folder1)
                .addToClassPath(folder2);

        assertCreates(suite, compositeOf(
                new FileNamePatternTestClassFinder("the pattern", folder1, cl),
                new FileNamePatternTestClassFinder("the pattern", folder2, cl)
        ));
    }


    private void assertCreates(SuiteConfigurationBuilder suite, TestClassFinder expected) {
        assertThat(TestClassFinderFactory.create(suite.freeze(), cl), is(expected));
    }

    private static CompositeTestClassFinder compositeOf(TestClassFinder... finders) {
        return new CompositeTestClassFinder(Arrays.asList(finders));
    }
}
