// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.config;

import org.hamcrest.*;
import org.junit.*;
import org.junit.internal.matchers.TypeSafeMatcher;

import java.nio.file.*;

import static fi.jumi.core.util.ReflectionUtil.getFieldValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SuiteConfigurationTest {

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


    // testClasses

    @Test
    public void test_classes_can_be_changed() {
        builder.testClasses("TheClass", "AnotherClass");

        assertThat(configuration().testClasses(), contains("TheClass", "AnotherClass"));
    }

    @Test
    public void test_classes_defaults_to_empty() {
        assertThat(configuration().testClasses(), is(empty()));
    }


    // testFileMatcher

    @Test
    public void test_files_matcher_can_be_changed() {
        builder.testFileMatcher("*Foo.class");

        assertThat(configuration().testFileMatcher(), is("*Foo.class"));
    }

    @Test
    public void test_files_matcher_default_matches_classes_ending_with_Test_in_all_packages() {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher(configuration().testFileMatcher());

        assertThat("should match just Test", matcher, matches(Paths.get("Test.class")));
        assertThat("should match Test suffix", matcher, matches(Paths.get("XTest.class")));
        assertThat("should not match Test prefix", matcher, not(matches(Paths.get("TestX.class"))));
        assertThat("should not match non-class files", matcher, not(matches(Paths.get("SomeTest.java"))));
        assertThat("should match in all packages", matcher, matches(Paths.get("com/example/SomeTest.class")));
    }


    // helpers

    private SuiteConfiguration configuration() {
        return builder.freeze();
    }

    private static Matcher<? super PathMatcher> matches(final Path path) {
        return new TypeSafeMatcher<PathMatcher>() {

            @Override
            public boolean matchesSafely(PathMatcher matcher) {
                return matcher.matches(path);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("matches ")
                        .appendValue(path);
            }

            @Override
            public void describeMismatch(Object matcher, Description description) {
                description.appendText("was ")
                        .appendValue(matcher)
                        .appendText(" with pattern ")
                        .appendValue(getFieldValue(matcher, "val$pattern"));
            }
        };
    }
}
