// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util.matchers;

import org.hamcrest.*;

public class FailureMessageFormatter extends TypeSafeMatcher<SelfDescribing> {
    private final Matcher<?> matcher;

    public FailureMessageFormatter(Matcher<?> matcher) {
        this.matcher = matcher;
    }

    protected boolean matchesSafely(SelfDescribing item) {
        return false;
    }

    public void describeTo(Description description) {
        description.appendDescriptionOf(matcher);
    }

    protected void describeMismatchSafely(SelfDescribing item, Description mismatchDescription) {
        mismatchDescription.appendDescriptionOf(item);
    }
}
