// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.util;

import org.junit.Test;

import java.nio.file.*;

import static fi.jumi.core.util.PathMatchers.matches;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

public class IncludeExcludePathMatcherTest {

    @Test
    public void matches_included_patterns() {
        IncludeExcludePathMatcher matcher = new IncludeExcludePathMatcher(FileSystems.getDefault(), "glob:*.txt", "");

        assertThat(matcher, matches(Paths.get("foo.txt")));
        assertThat(matcher, not(matches(Paths.get("foo.html"))));
    }

    @Test
    public void does_not_match_included_but_also_excluded_patterns() {
        IncludeExcludePathMatcher matcher = new IncludeExcludePathMatcher(FileSystems.getDefault(), "glob:*.txt", "glob:bar*");

        assertThat(matcher, matches(Paths.get("foo.txt")));
        assertThat(matcher, not(matches(Paths.get("bar.txt"))));
    }
}
