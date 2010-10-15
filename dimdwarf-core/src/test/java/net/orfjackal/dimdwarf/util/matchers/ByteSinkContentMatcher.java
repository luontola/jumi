// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util.matchers;

import net.orfjackal.dimdwarf.util.ByteSink;
import org.apache.mina.core.buffer.IoBuffer;
import org.hamcrest.*;

public class ByteSinkContentMatcher extends TypeSafeMatcher<ByteSink> {

    private final Matcher<IoBuffer> matcher;

    public ByteSinkContentMatcher(Matcher<IoBuffer> matcher) {
        this.matcher = matcher;
    }

    protected boolean matchesSafely(ByteSink sink) {
        return sink.matches(matcher);
    }

    public void describeTo(Description description) {
        description
                .appendText("a stream which ")
                .appendDescriptionOf(matcher);
    }

    protected void describeMismatchSafely(ByteSink item, Description mismatchDescription) {
        mismatchDescription
                .appendText("received ")
                .appendDescriptionOf(item);
    }
}
