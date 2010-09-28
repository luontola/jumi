// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util;

import org.hamcrest.*;

import java.util.concurrent.*;

public class EventSink<T> implements SelfDescribing {
    private final BlockingQueue<T> events = new LinkedBlockingQueue<T>();

    private final long timeout;

    public EventSink(long timeout) {
        this.timeout = timeout;
    }

    public void update(T event) {
        events.add(event);
    }

    public boolean firstEventMatches(Matcher<T> matcher) throws InterruptedException {
        Object first = events.poll(timeout, TimeUnit.MILLISECONDS);
        return matcher.matches(first);
    }

    public String toString() {
        StringDescription desc = new StringDescription();
        describeTo(desc);
        return desc.toString();
    }

    public void describeTo(Description description) {
        description.appendText("received events ").appendValue(events);
    }
}
