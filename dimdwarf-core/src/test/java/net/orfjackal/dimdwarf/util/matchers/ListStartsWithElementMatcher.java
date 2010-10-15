// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util.matchers;

import org.hamcrest.*;

import java.util.List;

public class ListStartsWithElementMatcher<T> extends TypeSafeMatcher<List<T>> {

    private final Matcher<T> firstElementMatcher;

    public ListStartsWithElementMatcher(Matcher<T> firstElementMatcher) {
        this.firstElementMatcher = firstElementMatcher;
    }

    protected boolean matchesSafely(List<T> item) {
        return !item.isEmpty()
                && firstElementMatcher.matches(item.get(0));
    }

    public void describeTo(Description description) {
        description
                .appendText("start with ")
                .appendDescriptionOf(firstElementMatcher);
    }
}
