// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.test.util;

import org.junit.*;

import java.io.*;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;

public class StreamWatcherTest {

    private PrintWriter stream;
    private StreamWatcher watcher;

    @Before
    public void setUp() throws IOException {
        PipedWriter w = new PipedWriter();
        PipedReader r = new PipedReader(w);
        stream = new PrintWriter(w);
        watcher = new StreamWatcher(r);
    }

    @After
    public void tearDown() throws IOException {
        stream.close();
    }

    @Test
    public void succeeds_when_the_stream_contains_the_expected_text() throws Exception {
        stream.println("some text");
        stream.close();

        watcher.waitForLineContaining("some", 1, TimeUnit.SECONDS);
    }

    @Test(expected = AssertionError.class)
    public void fails_when_the_stream_does_NOT_contain_the_expected_text() throws Exception {
        stream.println("some text");
        stream.close();

        watcher.waitForLineContaining("text not there", 1, TimeUnit.SECONDS);
    }

    @Test
    public void failure_message_has_the_text_which_the_stream_did_contain() throws Exception {
        stream.println("one");
        stream.println("two");
        stream.close();

        try {
            watcher.waitForLineContaining("three", 1, TimeUnit.SECONDS);
        } catch (AssertionError e) {
            assertThat(e.getMessage(), containsString("one\ntwo"));
        }
    }

    @Test
    public void after_one_success_the_next_time_reading_is_started_from_where_it_was_left() throws Exception {
        stream.println("one");
        stream.println("two");
        stream.println("three");
        stream.close();

        watcher.waitForLineContaining("one", 1, TimeUnit.SECONDS);
        watcher.waitForLineContaining("two", 1, TimeUnit.SECONDS);
        try {
            watcher.waitForLineContaining("two", 1, TimeUnit.SECONDS);
            fail("expected to not find the text");
        } catch (AssertionError e) {
        }
    }

    @Test(expected = AssertionError.class)
    public void times_out_if_the_text_is_not_found_within_the_time_limit() throws Exception {
        watcher.waitForLineContaining("not found", 1, TimeUnit.MILLISECONDS);
    }
}
