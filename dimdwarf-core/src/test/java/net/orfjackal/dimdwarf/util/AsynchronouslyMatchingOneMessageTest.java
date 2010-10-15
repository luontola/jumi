// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util;

import org.junit.*;
import org.junit.rules.ExpectedException;

import java.util.Arrays;

import static net.orfjackal.dimdwarf.util.CustomMatchers.*;
import static net.orfjackal.dimdwarf.util.TestUtil.runAsynchronously;
import static org.hamcrest.Matchers.is;

public class AsynchronouslyMatchingOneMessageTest {

    public static final long TIMEOUT_NEVER_REACHED = 100;
    public static final long TIMEOUT_IS_REACHED = 1;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void passes_when_a_matching_event_has_already_arrived() throws InterruptedException {
        EventSink<String> events = new EventSink<String>(TIMEOUT_NEVER_REACHED);

        events.append(Arrays.asList("event"));

        assertEventually(events, firstEvent(is("event")));
    }

    @Test
    public void passes_when_a_matching_event_arrives_asynchronously() throws InterruptedException {
        final EventSink<String> events = new EventSink<String>(TIMEOUT_NEVER_REACHED);

        runAsynchronously(new Runnable() {
            public void run() {
                events.append(Arrays.asList("event"));
            }
        });

        assertEventually(events, firstEvent(is("event")));
    }

    @Test
    public void fails_when_there_is_a_non_matching_event() throws Throwable {
        EventSink<String> events = new EventSink<String>(TIMEOUT_NEVER_REACHED);

        events.append(Arrays.asList("non matching event"));

        thrown.expect(AssertionError.class);
        thrown.expectMessage("\"event\"");                  // expected
        thrown.expectMessage("[\"non matching event\"]");   // actual
        assertEventually(events, firstEvent(is("event")));
    }

    @Test
    public void fails_due_to_timeout_when_there_are_no_events() throws InterruptedException {
        EventSink<String> events = new EventSink<String>(TIMEOUT_IS_REACHED);

        thrown.expect(AssertionError.class);
        thrown.expectMessage("\"event\"");      // expected
        thrown.expectMessage("[]");             // actual
        assertEventually(events, firstEvent(is("event")));
    }
}
