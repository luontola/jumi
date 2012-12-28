// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.files;

import fi.jumi.core.config.SuiteConfigurationBuilder;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TestClassFinderFactoryTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void when_testClasses_is_set_creates_an_enumerated_finder() {
        assertCreates(new SuiteConfigurationBuilder().testClasses("Foo", "Bar"),
                new EnumeratedTestClassFinder(Arrays.asList("Foo", "Bar"), null));
    }

    @Test
    public void when_testFileMatcher_is_set_creates_an_enumerated_finder() {
        assertCreates(new SuiteConfigurationBuilder().testFileMatcher("the pattern"),
                new FileNamePatternTestClassFinder("the pattern", null, null));
    }

    @Test
    public void testClasses_takes_precedence_over_testFileMatcher() {
        assertCreates(new SuiteConfigurationBuilder().testClasses("Foo", "Bar").testFileMatcher("the pattern"),
                new EnumeratedTestClassFinder(Arrays.asList("Foo", "Bar"), null));
    }

    @Test
    public void testClasses_or_testFileMatcher_is_required() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("testClasses and testFileMatcher were both empty");
        TestClassFinderFactory.create(new SuiteConfigurationBuilder().testFileMatcher("").freeze(), null);
    }


    private static void assertCreates(SuiteConfigurationBuilder suite, TestClassFinder expected) {
        assertThat(TestClassFinderFactory.create(suite.freeze(), null), is(expected));
    }
}
