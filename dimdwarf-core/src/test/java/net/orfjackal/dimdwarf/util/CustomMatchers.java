// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util;

import org.hamcrest.*;

public class CustomMatchers {

    public static <T> Matcher<T> instanceOf(final Class<?> expected) {
        return new TypeSafeMatcher<T>() {
            public boolean matchesSafely(T item) {
                return expected.isInstance(item);
            }

            public void describeTo(Description description) {
                description.appendText("an instance of ").appendText(expected.getName());
            }
        };
    }

    public static <T> Matcher<EventSink<T>> eventuallyFirstEvent(final Matcher<T> matcher) {
        return new TypeSafeMatcher<EventSink<T>>() {

            public boolean matchesSafely(EventSink<T> item) {
                try {
                    return item.firstEventMatches(matcher);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            public void describeTo(Description description) {
                // TODO
            }
        };
    }
}
