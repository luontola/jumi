// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util;

import net.orfjackal.dimdwarf.util.matchers.*;
import org.apache.mina.core.buffer.IoBuffer;
import org.hamcrest.Matcher;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

public class CustomMatchers {

    // TODO: matching messages in sequence
    // - add a consume/dropFirst method to AsynchronousSink, use it when assert passes
    // - create ConsumingMatcher which tells how many items it will consume

    @SuppressWarnings({"SynchronizationOnLocalVariableOrMethodParameter"})
    public static void assertEventually(AsynchronousSink<?> sink, Matcher<?> matcher) {
        synchronized (sink) {
            Timeout timeout = new Timeout(sink.getTimeout());
            if (!matchesWithinTimeout(sink, matcher, timeout)) {
                assertThat(sink, new FailureMessageFormatter(matcher));
            }
        }
    }

    private static boolean matchesWithinTimeout(AsynchronousSink<?> sink, Matcher<?> matcher, Timeout timeout) {
        while (!matcher.matches(sink.getContent())) {
            if (timeout.hasTimedOut()) {
                return false;
            }
            timeout.waitUntilTimeout(sink);
        }
        return true;
    }

    public static <T> Matcher<List<T>> firstEvent(Matcher<? super T> matcher) {
        return new ListStartsWithElementMatcher<T>(matcher);
    }

    public static Matcher<IoBuffer> startsWithBytes(IoBuffer expected) {
        return new IoBufferStartsWithBytesMatcher(expected);
    }
}
