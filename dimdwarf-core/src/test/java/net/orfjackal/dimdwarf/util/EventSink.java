// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util;

import org.hamcrest.*;

import java.util.*;

public class EventSink<T> extends AbstractSink<List<T>> implements SelfDescribing {

    private final List<T> events = new ArrayList<T>();

    public EventSink(long timeout) {
        super(timeout);
    }

    protected void doAppend(List<T> event) {
        events.addAll(event);
    }

    protected List<T> getContent() {
        return events;
    }

    public void describeTo(Description description) {
        description
                .appendText("events ")
                .appendValueList("[", ", ", "]", getContent());
    }
}
