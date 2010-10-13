// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util;

import org.hamcrest.*;

import java.util.concurrent.*;

public class EventSink<T> extends AbstractSink<T> {

    // TODO: could be replaced with a non-synchronized collection
    private final BlockingQueue<T> events = new LinkedBlockingQueue<T>();

    public EventSink(long timeout) {
        super(timeout);
    }

    protected void doAppend(T event) {
        events.add(event);
    }

    protected boolean doMatch(Matcher<?> matcher) {
        if (events.isEmpty()) {
            return false;
        }
        Object first = events.peek();
        return matcher.matches(first);
    }

    public String toString() {
        StringDescription desc = new StringDescription();
        desc.appendText("received events ").appendValue(events);
        return desc.toString();
    }
}
