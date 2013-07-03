// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.output;

import com.google.common.base.Throwables;
import fi.jumi.core.util.ConcurrencyUtil;
import org.apache.commons.io.output.WriterOutputStream;
import org.junit.*;
import org.junit.rules.Timeout;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static fi.jumi.core.util.ConcurrencyUtil.*;
import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class OutputCapturerTest {

    private static final int TIMEOUT = 1000;

    @Rule
    public final Timeout timeout = new Timeout(TIMEOUT);

    private final StringWriter printedToOut = new StringWriter();
    private final StringWriter printedToErr = new StringWriter();
    private final OutputStream realOut = new WriterOutputStream(printedToOut);
    private final OutputStream realErr = new WriterOutputStream(printedToErr);

    private final OutputCapturer capturer = new OutputCapturer(realOut, realErr, Charset.defaultCharset());


    // basic capturing

    @Test
    public void passes_through_stdout_to_the_real_stdout() {
        capturer.out().print("foo");

        assertThat("stdout", printedToOut.toString(), is("foo"));
        assertThat("stderr", printedToErr.toString(), is(""));
    }

    @Test
    public void passes_through_stderr_to_the_real_stderr() {
        capturer.err().print("foo");

        assertThat("stdout", printedToOut.toString(), is(""));
        assertThat("stderr", printedToErr.toString(), is("foo"));
    }

    @Test
    public void captures_stdout() {
        OutputListenerSpy listener = new OutputListenerSpy();

        capturer.captureTo(listener);
        capturer.out().print("foo");

        assertThat(listener.out).as("stdout").containsExactly("foo");
        assertThat(listener.err).as("stderr").containsExactly();
    }

    @Test
    public void captures_stderr() {
        OutputListenerSpy listener = new OutputListenerSpy();

        capturer.captureTo(listener);
        capturer.err().print("foo");

        assertThat(listener.out).as("stdout").containsExactly();
        assertThat(listener.err).as("stderr").containsExactly("foo");
    }

    @Test
    public void single_byte_prints_are_also_captured_and_passed_through() {
        OutputListenerSpy listener = new OutputListenerSpy();
        capturer.captureTo(listener);

        capturer.out().write('.');

        assertThat(printedToOut.toString(), is("."));
        assertThat(listener.out).containsExactly(".");
    }

    @Test
    public void after_starting_a_new_capture_all_new_events_to_to_the_new_output_listener() {
        OutputListenerSpy listener1 = new OutputListenerSpy();
        OutputListenerSpy listener2 = new OutputListenerSpy();

        capturer.captureTo(listener1);
        capturer.captureTo(listener2);
        capturer.out().print("foo");

        assertThat(listener1.out).containsExactly();
        assertThat(listener2.out).containsExactly("foo");
    }

    @Test
    public void starting_a_new_capture_does_not_require_installing_a_new_PrintStream_to_SystemOut() {
        OutputListenerSpy listener = new OutputListenerSpy();

        PrintStream out = capturer.out();
        capturer.captureTo(listener);
        out.print("foo");

        assertThat(listener.out).containsExactly("foo");
    }


    // concurrency

    @Test
    public void concurrent_captures_are_isolated_from_each_other() throws InterruptedException {
        final CountDownLatch barrier = new CountDownLatch(2);
        final OutputListenerSpy listener1 = new OutputListenerSpy();
        final OutputListenerSpy listener2 = new OutputListenerSpy();

        runConcurrently(
                new Runnable() {
                    @Override
                    public void run() {
                        capturer.captureTo(listener1);
                        sync(barrier);
                        capturer.out().print("from thread 1");
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        capturer.captureTo(listener2);
                        sync(barrier);
                        capturer.out().print("from thread 2");
                    }
                }
        );

        assertThat(listener1.out).containsExactly("from thread 1");
        assertThat(listener2.out).containsExactly("from thread 2");
    }

    @Test
    public void captures_what_is_printed_in_spawned_threads() throws InterruptedException {
        OutputListenerSpy listener = new OutputListenerSpy();

        capturer.captureTo(listener);
        runConcurrently(new Runnable() {
            @Override
            public void run() {
                capturer.out().print("from spawned thread");
            }
        });

        assertThat(listener.out).containsExactly("from spawned thread");
    }

    @Test
    public void when_spawned_threads_print_something_after_the_capture_ends_they_are_still_include_in_the_original_capture() throws InterruptedException {
        final CountDownLatch beforeFinished = new CountDownLatch(2);
        final CountDownLatch afterFinished = new CountDownLatch(2);
        OutputListenerSpy capture1 = new OutputListenerSpy();
        OutputListenerSpy capture2 = new OutputListenerSpy();

        capturer.captureTo(capture1);
        Thread t = startThread(new Runnable() {
            @Override
            public void run() {
                capturer.out().print("before capture finished");
                sync(beforeFinished);
                sync(afterFinished);
                capturer.out().print("after capture finished");
            }
        });
        sync(beforeFinished);
        capturer.captureTo(capture2);
        sync(afterFinished);
        t.join();

        assertThat(capture1.out).containsExactly("before capture finished", "after capture finished");
        assertThat(capture2.out).containsExactly();
    }

    /**
     * {@link PrintStream} synchronizes all its operations on itself, but since {@link PrintStream#println} does two
     * calls to the underlying {@link OutputStream} (or if the printed text is longer than all the internal buffers),
     * it's possible for stdout and stderr to get interleaved.
     */
    @Test
    public void printing_to_stdout_and_stderr_concurrently() throws InterruptedException {
        final int ITERATIONS = 30;
        CombinedOutput combinedOutput = new CombinedOutput();
        capturer.captureTo(combinedOutput);

        runConcurrently(
                new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < ITERATIONS; i++) {
                            capturer.out().println("O");
                        }
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < ITERATIONS; i++) {
                            capturer.err().println("E");
                        }
                    }
                }
        );

        assertThat(combinedOutput.toString()).matches("(O\\r?\\n|E\\r?\\n)+");
    }

    /**
     * {@link Throwable#printStackTrace} synchronizes on {@code System.err}, but it can still interleave with something
     * that is printed to {@code System.out}. We can fix that by synchronizing all printing on {@code System.err}, but
     * only in one direction; the output from {@code Throwable.printStackTrace(System.out)} may still interleave with
     * printing to {@code System.err}.
     */
    @Test
    public void printing_a_stack_trace_to_stderr_and_normally_to_stdout_concurrently() throws InterruptedException {
        final CountDownLatch isPrintingToOut = new CountDownLatch(1);
        final CountDownLatch hasPrintedStackTrace = new CountDownLatch(1);
        final Exception exception = new Exception("dummy exception");
        CombinedOutput combinedOutput = new CombinedOutput();
        capturer.captureTo(combinedOutput);

        runConcurrently(
                new Runnable() {
                    @Override
                    public void run() {
                        await(isPrintingToOut);
                        exception.printStackTrace(capturer.err());
                        hasPrintedStackTrace.countDown();
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        while (hasPrintedStackTrace.getCount() > 0) {
                            capturer.out().println("*garbage*");
                            isPrintingToOut.countDown();
                        }
                    }
                }
        );

        assertThat(combinedOutput.toString(), containsString(Throwables.getStackTraceAsString(exception)));
    }


    // helpers

    private static void sync(CountDownLatch barrier) {
        ConcurrencyUtil.sync(barrier, TIMEOUT);
    }

    private static void await(CountDownLatch barrier) {
        ConcurrencyUtil.await(barrier, TIMEOUT);
    }

    private static class OutputListenerSpy implements OutputListener {
        public List<String> out = Collections.synchronizedList(new ArrayList<String>());
        public List<String> err = Collections.synchronizedList(new ArrayList<String>());

        @Override
        public void out(String text) {
            out.add(text);
        }

        @Override
        public void err(String text) {
            err.add(text);
        }
    }

    private static class CombinedOutput implements OutputListener {
        private final StringBuffer sb = new StringBuffer();

        @Override
        public void out(String text) {
            sb.append(text);
        }

        @Override
        public void err(String text) {
            sb.append(text);
        }

        @Override
        public String toString() {
            return sb.toString();
        }
    }
}
