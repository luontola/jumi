// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.util;

import org.hamcrest.*;

public class StringMatchers {

    public static Matcher<String> containsSubStrings(final String... expectedSubStrings) {
        return new TypeSafeMatcher<String>() {
            @Override
            protected boolean matchesSafely(String item) {
                return Strings.containsSubStrings(item, expectedSubStrings);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("contains sub strings ").appendValueList("", ", ", "", expectedSubStrings);
            }
        };
    }

    public static Matcher<String> hasOccurrences(final int expectedOccurrences, final String needle) {
        return new TypeSafeMatcher<String>() {
            @Override
            protected boolean matchesSafely(String haystack) {
                return Strings.countOccurrences(haystack, needle) == expectedOccurrences;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("has ")
                        .appendValue(expectedOccurrences)
                        .appendText(" occurrences of ")
                        .appendValue(needle);
            }

            @Override
            protected void describeMismatchSafely(String haystack, Description mismatchDescription) {
                int occurrences = Strings.countOccurrences(haystack, needle);
                mismatchDescription.appendText("was ")
                        .appendValue(occurrences)
                        .appendText(" occurrences in:\n")
                        .appendText(haystack);
            }
        };
    }
}
