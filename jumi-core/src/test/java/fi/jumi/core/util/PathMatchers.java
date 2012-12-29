// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.util;

import org.hamcrest.*;
import org.junit.internal.matchers.TypeSafeMatcher;

import java.nio.file.*;

public class PathMatchers {

    public static Matcher<? super PathMatcher> matches(final Path path) {
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
        };
    }
}
