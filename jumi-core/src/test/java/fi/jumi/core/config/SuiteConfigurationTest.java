// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.config;

import fi.jumi.core.discovery.dummies.*;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.nio.file.*;

import static fi.jumi.core.util.PathMatchers.matches;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SuiteConfigurationTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private SuiteConfigurationBuilder builder = new SuiteConfigurationBuilder();

    @Before
    public void setup() {
        // make sure that melting makes all fields back mutable
        builder = builder.freeze().melt();
    }


    // classPath (deprecated 2013-08-12; renamed the property from "classPath" to "classpath")

    @Deprecated
    @Test
    public void class_path_can_be_changed__Path() {
        builder.setClassPath(Paths.get("old.jar"))
                .setClassPath(Paths.get("foo.jar"), Paths.get("bar.jar"));

        assertThat(configuration().getClassPath(), is(asList(Paths.get("foo.jar").toUri(), Paths.get("bar.jar").toUri())));
    }

    @Deprecated
    @Test
    public void class_path_can_be_changed__URI() {
        builder.setClassPath(Paths.get("old.jar").toUri())
                .setClassPath(Paths.get("foo.jar").toUri(), Paths.get("bar.jar").toUri());

        assertThat(configuration().getClassPath(), is(asList(Paths.get("foo.jar").toUri(), Paths.get("bar.jar").toUri())));
    }

    @Deprecated
    @Test
    public void class_path_can_be_appended_to() {
        builder.addToClassPath(Paths.get("foo.jar"))
                .addToClassPath(Paths.get("bar.jar"));

        assertThat(configuration().getClassPath(), is(asList(Paths.get("foo.jar").toUri(), Paths.get("bar.jar").toUri())));
    }

    @Deprecated
    @Test
    public void class_path_defaults_to_empty() {
        assertThat(configuration().getClassPath(), is(empty()));
    }


    // classpath

    @Test
    public void classpath_can_be_changed__Path() {
        builder.setClasspath(Paths.get("old.jar"))
                .setClasspath(Paths.get("foo.jar"), Paths.get("bar.jar"));

        assertThat(configuration().getClasspath(), is(asList(Paths.get("foo.jar").toUri(), Paths.get("bar.jar").toUri())));
    }

    @Test
    public void classpath_can_be_changed__URI() {
        builder.setClasspath(Paths.get("old.jar").toUri())
                .setClasspath(Paths.get("foo.jar").toUri(), Paths.get("bar.jar").toUri());

        assertThat(configuration().getClasspath(), is(asList(Paths.get("foo.jar").toUri(), Paths.get("bar.jar").toUri())));
    }

    @Test
    public void classpath_can_be_appended_to() {
        builder.addToClasspath(Paths.get("foo.jar"))
                .addToClasspath(Paths.get("bar.jar"));

        assertThat(configuration().getClasspath(), is(asList(Paths.get("foo.jar").toUri(), Paths.get("bar.jar").toUri())));
    }

    @Test
    public void classpath_defaults_to_empty() {
        assertThat(configuration().getClasspath(), is(empty()));
    }


    // jvmOptions

    @Test
    public void jvm_options_can_be_changed() {
        builder.setJvmOptions("-old")
                .setJvmOptions("-new", "options");

        assertThat(configuration().getJvmOptions(), is(asList("-new", "options")));
    }

    @Test
    public void jvm_options_can_be_appended_to() {
        builder.addJvmOptions("-option")
                .addJvmOptions("-more", "options");

        assertThat(configuration().getJvmOptions(), is(asList("-option", "-more", "options")));
    }

    @Test
    public void jvm_options_defaults_to_empty() {
        assertThat(configuration().getJvmOptions(), is(empty()));
    }


    // workingDirectory

    @Test
    public void working_directory_can_be_changed() {
        builder.setWorkingDirectory(Paths.get("foo"));

        assertThat(configuration().getWorkingDirectory(), is(Paths.get("foo").toAbsolutePath().toUri()));
    }

    @Test
    public void working_directory_defaults_to_current_working_directory() throws IOException {
        assertThat(configuration().getWorkingDirectory(), is(Paths.get(".").toRealPath().toUri()));
    }


    // includedTestsPattern & excludedTestsPattern

    @Test
    public void included_tests_pattern_can_be_changed() {
        builder.setIncludedTestsPattern("glob:*Foo.class");

        assertThat(configuration().getIncludedTestsPattern(), is("glob:*Foo.class"));
    }

    @Test
    public void disallows_invalid_included_tests_pattern() {
        thrown.expect(IllegalArgumentException.class);
        builder.setIncludedTestsPattern("garbage");
    }

    @Test
    public void excluded_tests_pattern_can_be_changed() {
        builder.setExcludedTestsPattern("glob:*Bar.class");

        assertThat(configuration().getExcludedTestsPattern(), is("glob:*Bar.class"));
    }

    @Test
    public void disallows_invalid_excluded_tests_pattern() {
        thrown.expect(IllegalArgumentException.class);
        builder.setExcludedTestsPattern("garbage");
    }

    @Test
    public void excluded_tests_pattern_can_be_disabled_by_setting_it_empty() {
        builder.setExcludedTestsPattern("");

        PathMatcher matcher = configuration().createTestFileMatcher(FileSystems.getDefault());
        assertThat("shouldn't anymore exclude inner classes", matcher, matches(Paths.get("Test$Test.class")));
    }

    @Test
    public void test_file_matcher_by_default_matches_classes_ending_with_Test_in_all_packages() {
        PathMatcher matcher = configuration().createTestFileMatcher(FileSystems.getDefault());

        assertThat("should match just Test", matcher, matches(Paths.get("Test.class")));
        assertThat("should match Test suffix", matcher, matches(Paths.get("XTest.class")));
        assertThat("should not match Test prefix", matcher, not(matches(Paths.get("TestX.class"))));
        assertThat("should not match non-class files", matcher, not(matches(Paths.get("SomeTest.java"))));
        assertThat("should match in all packages", matcher, matches(Paths.get("com/example/SomeTest.class")));
        assertThat("should not match inner classes", matcher, not(matches(Paths.get("Test$Test.class"))));
        assertThat("should not match inner classes in any package", matcher, not(matches(Paths.get("com/example/Test$Test.class"))));
    }

    @Test
    public void convenience_method_for_running_specific_test_classes__String() {
        builder.setTestClasses("TheClass", "com.example.AnotherClass");

        SuiteConfiguration suite = configuration();
        assertThat(suite.getIncludedTestsPattern(), is("glob:{TheClass.class,com/example/AnotherClass.class}"));
        assertThat(suite.getExcludedTestsPattern(), is(""));
    }

    @Test
    public void convenience_method_for_running_specific_test_classes__Class() {
        builder.setTestClasses(DummyTest.class, AnotherDummyTest.class);

        SuiteConfiguration suite = configuration();
        assertThat(suite.getIncludedTestsPattern(), containsString("DummyTest"));
        assertThat(suite.getIncludedTestsPattern(), containsString("AnotherDummyTest"));
        assertThat(suite.getExcludedTestsPattern(), is(""));
    }


    // helpers

    private SuiteConfiguration configuration() {
        return builder.freeze();
    }
}
