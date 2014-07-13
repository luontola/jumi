// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.util;

import com.google.common.base.Predicate;
import org.hamcrest.*;

public class PredicateMatchers {

    public static <T> Matcher<T> satisfies(Predicate<T> predicate) {
        return new TypeSafeMatcher<T>() {
            @Override
            protected boolean matchesSafely(T item) {
                return predicate.apply(item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("satisfies predicate");
            }
        };
    }
}
