// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util;

import org.apache.mina.core.buffer.IoBuffer;
import org.hamcrest.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CustomMatchers {

    public static <T> void assertEventually(EventSink<T> sink, Matcher<? super EventSink<T>> matcher) {
        assertThat(sink, matcher);
    }

    public static void assertEventually(ByteSink sink, Matcher<ByteSink> matcher) {
        assertThat(sink, matcher);
    }

    public static <T> Matcher<EventSink<? super T>> firstEvent(final Matcher<? super T> matcher) {
        return new TypeSafeMatcher<EventSink<? super T>>() {

            protected boolean matchesSafely(EventSink<? super T> item) {
                return item.matches(matcher);
            }

            public void describeTo(Description description) {
                description
                        .appendText("first event ")
                        .appendDescriptionOf(matcher);
            }

            protected void describeMismatchSafely(EventSink<? super T> item, Description mismatchDescription) {
                mismatchDescription
                        .appendText("received ")
                        .appendDescriptionOf(item);
            }
        };
    }

    public static Matcher<ByteSink> startsWithBytes(final IoBuffer expected) {
        return new TypeSafeMatcher<ByteSink>() {
            protected boolean matchesSafely(ByteSink item) {
                return item.matches(is(expected));
            }

            public void describeTo(Description description) {
                description
                        .appendText("starts with " + expected.remaining() + " bytes: ")
                        .appendText(expected.getHexDump());

            }

            protected void describeMismatchSafely(ByteSink item, Description mismatchDescription) {
                mismatchDescription
                        .appendText("received ")
                        .appendDescriptionOf(item);
            }
        };
    }

    // TODO: move inner classes to upper level
}
