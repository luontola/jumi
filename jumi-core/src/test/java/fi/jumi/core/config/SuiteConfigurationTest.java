// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.config;

import org.junit.*;
import org.junit.rules.ExpectedException;

import java.nio.file.*;

import static fi.jumi.core.util.PathMatchers.matches;
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


    // classPath

    @Test
    public void class_path_can_be_changed() {
        builder.addToClassPath(Paths.get("foo.jar"));

        assertThat(configuration().classPath(), contains(Paths.get("foo.jar").toUri()));
    }

    @Test
    public void class_path_defaults_to_empty() {
        assertThat(configuration().classPath(), is(empty()));
    }


    // jvmOptions

    @Test
    public void jvm_options_can_be_changed() {
        builder.addJvmOptions("-option");

        assertThat(configuration().jvmOptions(), contains("-option"));
    }

    @Test
    public void jvm_options_defaults_to_empty() {
        assertThat(configuration().jvmOptions(), is(empty()));
    }


    // includedTestsPattern & excludedTestsPattern

    @Test
    public void included_tests_pattern_can_be_changed() {
        builder.includedTestsPattern("glob:*Foo.class");

        assertThat(configuration().includedTestsPattern(), is("glob:*Foo.class"));
    }

    @Test
    public void disallows_invalid_included_tests_pattern() {
        thrown.expect(IllegalArgumentException.class);
        builder.includedTestsPattern("garbage");
    }

    @Test
    public void excluded_tests_pattern_can_be_changed() {
        builder.excludedTestsPattern("glob:*Bar.class");

        assertThat(configuration().excludedTestsPattern(), is("glob:*Bar.class"));
    }

    @Test
    public void disallows_invalid_excluded_tests_pattern() {
        thrown.expect(IllegalArgumentException.class);
        builder.excludedTestsPattern("garbage");
    }

    @Test
    public void excluded_tests_pattern_can_be_disabled_by_setting_it_empty() {
        builder.excludedTestsPattern("");

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
    public void convenience_method_for_running_specific_test_classes() {
        builder.testClasses("TheClass", "com.example.AnotherClass");

        SuiteConfiguration suite = configuration();
        assertThat(suite.includedTestsPattern(), is("glob:{TheClass.class,com/example/AnotherClass.class}"));
        assertThat(suite.excludedTestsPattern(), is(""));
    }


    // helpers

    private SuiteConfiguration configuration() {
        return builder.freeze();
    }
}
