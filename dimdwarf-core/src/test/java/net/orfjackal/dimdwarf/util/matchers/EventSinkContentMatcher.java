// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util.matchers;

import net.orfjackal.dimdwarf.util.EventSink;
import org.hamcrest.*;

import java.util.List;

public class EventSinkContentMatcher<T> extends TypeSafeMatcher<EventSink<? super T>> {

    private final Matcher<List<T>> matcher;

    public EventSinkContentMatcher(Matcher<List<T>> matcher) {
        this.matcher = matcher;
    }

    protected boolean matchesSafely(EventSink<? super T> item) {
        return item.matches(matcher);
    }

    public void describeTo(Description description) {
        description
                .appendText("events which ")
                .appendDescriptionOf(matcher);
    }

    protected void describeMismatchSafely(EventSink<? super T> item, Description mismatchDescription) {
        mismatchDescription
                .appendText("received ")
                .appendDescriptionOf(item);
    }
}
