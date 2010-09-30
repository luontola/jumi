// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util;

import org.hamcrest.*;

import static org.hamcrest.MatcherAssert.assertThat;

public class CustomMatchers {

    public static <T> void assertEventually(EventSink<T> events, Matcher<? super EventSink<T>> matcher) {
        assertThat(events, matcher);
    }

    public static <T> Matcher<EventSink<? super T>> firstEvent(final Matcher<? super T> matcher) {
        return new TypeSafeMatcher<EventSink<? super T>>() {

            protected boolean matchesSafely(EventSink<? super T> item) {
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
