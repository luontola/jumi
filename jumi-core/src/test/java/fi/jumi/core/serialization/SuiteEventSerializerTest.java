// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.serialization;

import fi.jumi.api.drivers.TestId;
import fi.jumi.core.api.*;
import fi.jumi.core.ipc.*;
import fi.jumi.core.util.SpyListener;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;

import static fi.jumi.core.util.EqualityMatchers.deepEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SuiteEventSerializerTest {

    @Test
    public void serializes_and_deserializes_all_events() {
        SpyListener<SuiteListener> spy = new SpyListener<>(SuiteListener.class);
        exampleUsage(spy.getListener());
        spy.replay();
        IpcBuffer buffer = new IpcBuffer(new AllocatedByteBufferSequence(1024));

        // serialize
        exampleUsage(new SuiteEventSerializer(buffer));

        // deserialize
        buffer.position(0);
        SuiteEventSerializer.deserialize(buffer, spy.getListener());

        spy.verify();
    }

    @Test
    public void example_usage_invokes_every_method_in_the_interface() {
        SuiteListenerSpy spy = new SuiteListenerSpy();

        exampleUsage(spy);

        for (Method method : SuiteListener.class.getMethods()) {
            assertThat("invoked methods", spy.methodInvocations.keySet(), hasItem(method));
        }
    }

    private static void exampleUsage(SuiteListener listener) {
        TestFile testFile = TestFile.fromClassName("com.example.SampleTest");
        RunId runId = new RunId(1);

        listener.onSuiteStarted();
        listener.onTestFileFound(testFile);
        listener.onAllTestFilesFound();

        listener.onTestFound(testFile, TestId.ROOT, "SampleTest");
        listener.onTestFound(testFile, TestId.of(0), "testName");

        listener.onRunStarted(runId, testFile);
        listener.onTestStarted(runId, TestId.ROOT);
        listener.onTestStarted(runId, TestId.of(0));
        listener.onPrintedOut(runId, "printed to out");
        listener.onPrintedErr(runId, "printed to err");
        listener.onFailure(runId, StackTrace.from(new AssertionError("assertion message"))); // TODO: add cause and suppressed, also test the stack trace elements
        listener.onTestFinished(runId);
        listener.onTestFinished(runId);
        listener.onRunFinished(runId);

        listener.onInternalError("error message", StackTrace.from(new Exception("exception message")));
        listener.onTestFileFinished(testFile);
        listener.onSuiteFinished();
    }


    // StackTrace

    @Test
    public void test_serialization_of_StackTrace() {
        StackTrace original = StackTrace.from(new IOException("the message"));

        assertThat(serializeAndDeserialize(original), is(deepEqualTo(original)));
    }

    @Test
    public void test_serialization_of_StackTrace_with_causes() {
        StackTrace original = StackTrace.from(
                new IOException("the message",
                        new IllegalArgumentException("cause 1",
                                new IllegalStateException("cause 2"))));

        assertThat(serializeAndDeserialize(original), is(deepEqualTo(original)));
    }

    @Test
    public void test_serialization_of_StackTrace_with_suppressed() {
        IOException e = new IOException("the message");
        e.addSuppressed(new IllegalArgumentException("suppressed 1"));
        e.addSuppressed(new IllegalStateException("suppressed 2"));
        StackTrace original = StackTrace.from(e);

        assertThat(serializeAndDeserialize(original), is(deepEqualTo(original)));
    }

    private static StackTrace serializeAndDeserialize(StackTrace expected) {
        IpcBuffer buffer = new IpcBuffer(new AllocatedByteBufferSequence(1024));
        new SuiteEventSerializer(buffer).writeStackTrace(expected);
        buffer.position(0);
        return SuiteEventSerializer.readStackTrace(buffer);
    }


    // String

    @Test
    public void test_serialization_of_String() {
        assertThat("empty string", serializeAndDeserialize(""), is(""));

        String original = RandomStringUtils.random(10);
        assertThat("random string", serializeAndDeserialize(original), is(original));
    }

    @Test
    public void test_serialization_of_String_with_non_printable_characters() {
        for (char c = 0; c < ' '; c++) {
            String original = "" + c;
            assertThat("0x" + Integer.toHexString(c), serializeAndDeserialize(original), is(original));
        }
    }

    private static String serializeAndDeserialize(String original) {
        IpcBuffer buffer = new IpcBuffer(new AllocatedByteBufferSequence(16));
        new SuiteEventSerializer(buffer).writeString(original);
        buffer.position(0);
        return SuiteEventSerializer.readString(buffer);
    }
}
