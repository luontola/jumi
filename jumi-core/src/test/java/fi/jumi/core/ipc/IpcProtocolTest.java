// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.queue.MessageSender;
import fi.jumi.api.drivers.TestId;
import fi.jumi.core.api.*;
import fi.jumi.core.events.SuiteListenerEventizer;
import fi.jumi.core.ipc.buffer.*;
import fi.jumi.core.runs.RunIdSequence;
import fi.jumi.core.util.SpyListener;
import org.junit.*;
import org.junit.rules.*;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.concurrent.locks.LockSupport;

import static fi.jumi.core.util.ConcurrencyUtil.runConcurrently;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.mock;

public class IpcProtocolTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Rule
    public final TemporaryFolder tempDir = new TemporaryFolder();


    @Test
    public void encodes_and_decodes_all_events() {
        SpyListener<SuiteListener> spy = new SpyListener<>(SuiteListener.class);
        exampleUsage(spy.getListener());
        spy.replay();
        IpcBuffer buffer = TestUtil.newIpcBuffer();

        // encode
        IpcProtocol<SuiteListener> protocol = newIpcProtocol(buffer);
        protocol.start();
        exampleUsage(sendTo(protocol));
        protocol.end();

        // decode
        buffer.position(0);
        decodeAll(protocol, spy.getListener());

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

    @Test(timeout = 5000)
    public void test_concurrent_producer_and_consumer() throws Exception {
        SpyListener<SuiteListener> expectations = new SpyListener<>(SuiteListener.class);
        lotsOfEventsForConcurrencyTesting(expectations.getListener(), 0);
        expectations.replay();

        Path mmf = tempDir.getRoot().toPath().resolve("mmf");

        Runnable producer = () -> {
            IpcBuffer buffer = new IpcBuffer(new MappedByteBufferSequence(new FileSegmenter(mmf, 4 * 1024, 512 * 1024)));

            IpcProtocol<SuiteListener> protocol = newIpcProtocol(buffer);
            protocol.start();
            lotsOfEventsForConcurrencyTesting(sendTo(protocol), 1);
            protocol.end();
        };

        Runnable consumer = () -> {
            IpcBuffer buffer = new IpcBuffer(new MappedByteBufferSequence(
                    new FileSegmenter(mmf, 1024, 1024))); // different segment size, because the reader should not create new segments

            decodeAll(newIpcProtocol(buffer), expectations.getListener());
        };

        runConcurrently(producer, consumer);

        expectations.verify();
    }

    private static void lotsOfEventsForConcurrencyTesting(SuiteListener listener, int nanosToPark) {
        TestFile testFile = TestFile.fromClassName("DummyTest");
        RunIdSequence runIds = new RunIdSequence();
        for (int i = 0; i < 10; i++) {
            RunId runId = runIds.nextRunId();

            // Not a realistic scenario, because we are only interested in concurrency testing
            // the IPC protocol and not the specifics of a particular interface.
            listener.onSuiteStarted();
            LockSupport.parkNanos(nanosToPark);
            listener.onRunStarted(runId, testFile);
            LockSupport.parkNanos(nanosToPark);
            listener.onRunFinished(runId);
            LockSupport.parkNanos(nanosToPark);
            listener.onSuiteFinished();
            LockSupport.parkNanos(nanosToPark);
        }
    }


    // headers

    @Test
    public void cannot_decode_if_header_has_wrong_magic_bytes() {
        IpcBuffer buffer = encodeSomeEvents();

        buffer.setInt(0, 0x0A0B0C0D);

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("wrong header: expected 4A 75 6D 69 but was 0A 0B 0C 0D");
        tryToDecode(buffer);
    }

    @Test
    public void cannot_decode_if_header_has_wrong_protocol_version() {
        IpcBuffer buffer = encodeSomeEvents();

        buffer.setInt(4, 9999);

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("unsupported protocol version: 9999");
        tryToDecode(buffer);
    }

    @Test
    public void cannot_decode_if_header_has_wrong_interface() {
        IpcBuffer buffer = encodeSomeEvents();

        buffer.position(8);
        StringEncoding.writeString(buffer, "com.example.AnotherInterface");

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("wrong interface: expected fi.jumi.core.api.SuiteListener but was com.example.AnotherInterface");
        tryToDecode(buffer);
    }

    @Test
    public void cannot_decode_if_header_has_wrong_interface_version() {
        IpcBuffer buffer = encodeSomeEvents();

        buffer.position(8);
        StringEncoding.readString(buffer); // go to interface version's position
        buffer.writeInt(9999);

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("unsupported interface version: 9999");
        tryToDecode(buffer);
    }

    private static IpcBuffer encodeSomeEvents() {
        IpcBuffer buffer = TestUtil.newIpcBuffer();
        IpcProtocol<SuiteListener> protocol = newIpcProtocol(buffer);
        protocol.start();
        sendTo(protocol).onSuiteStarted();
        protocol.end();
        return buffer;
    }

    private static void tryToDecode(IpcBuffer buffer) {
        buffer.position(0);
        IpcProtocol<SuiteListener> protocol = newIpcProtocol(buffer);
        decodeAll(protocol, mock(SuiteListener.class));
    }

    public static <T> void decodeAll(IpcProtocol<T> protocol, T target) {
        while (!Thread.interrupted()) {
            DecodeResult result = protocol.decodeNextMessage(target);
            if (result == DecodeResult.FINISHED) {
                return;
            }
        }
    }

    private static IpcProtocol<SuiteListener> newIpcProtocol(IpcBuffer buffer) {
        return new IpcProtocol<>(buffer, SuiteListenerEncoding::new);
    }

    private static SuiteListener sendTo(MessageSender<Event<SuiteListener>> target) {
        return new SuiteListenerEventizer().newFrontend(target);
    }
}
