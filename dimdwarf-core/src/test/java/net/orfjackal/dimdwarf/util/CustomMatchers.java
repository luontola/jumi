// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util;

import net.orfjackal.dimdwarf.util.matchers.*;
import org.apache.mina.core.buffer.IoBuffer;
import org.hamcrest.Matcher;

import java.util.List;

public class CustomMatchers {

    public static <T> void assertEventually(AsynchronousSink<T> sink, Matcher<?> matcher) {
        sink.assertEventually(matcher);
    }

    public static <T> Matcher<List<T>> firstEvent(Matcher<? super T> matcher) {
        return new ListStartsWithElementMatcher<T>(matcher);
    }

    public static Matcher<IoBuffer> startsWithBytes(IoBuffer expected) {
        return new IoBufferStartsWithBytesMatcher(expected);
    }
}
