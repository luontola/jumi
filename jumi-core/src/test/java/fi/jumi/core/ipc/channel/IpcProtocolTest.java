// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc.channel;

import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.queue.MessageSender;
import fi.jumi.core.Timeouts;
import fi.jumi.core.api.*;
import fi.jumi.core.events.SuiteListenerEventizer;
import fi.jumi.core.ipc.TestUtil;
import fi.jumi.core.ipc.buffer.*;
import fi.jumi.core.ipc.encoding.*;
import fi.jumi.core.runs.RunIdSequence;
import fi.jumi.core.util.SpyListener;
import org.junit.*;
import org.junit.rules.*;

import java.nio.file.*;
import java.util.concurrent.locks.LockSupport;

import static fi.jumi.core.util.ConcurrencyUtil.runConcurrently;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class IpcProtocolTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Rule
    public final TemporaryFolder tempDir = new TemporaryFolder();

    @Rule
    public final Timeout timeout = Timeouts.forUnitTest();

    @Test
    public void test_concurrent_producer_and_consumer() throws Exception {
        Path mmf = tempDir.getRoot().toPath().resolve("mmf");
        SpyListener<SuiteListener> expectations = new SpyListener<>(SuiteListener.class);
        lotsOfEventsForConcurrencyTesting(expectations.getListener(), 0);
        expectations.replay();

        Runnable producer = () -> {
            IpcWriter<SuiteListener> writer = IpcChannel.writer(mmf, SuiteListenerEncoding::new);
            lotsOfEventsForConcurrencyTesting(sendTo(writer), 1);
            writer.close();
        };
        Runnable consumer = () -> {
            IpcReader<SuiteListener> reader = IpcChannel.reader(mmf, SuiteListenerEncoding::new);
            TestUtil.decodeAll(reader, expectations.getListener());
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

    @Test
    public void producer_will_always_decide_segment_size_except_for_the_first_segment() throws Exception {
        Path mmf = tempDir.getRoot().toPath().resolve("mmf");
        SpyListener<SuiteListener> expectations = new SpyListener<>(SuiteListener.class);
        smallEventsForSegmentSizeConcurrencyTesting(expectations.getListener(), 0);
        expectations.replay();

        Runnable producer = () -> {
            IpcWriter<SuiteListener> writer = IpcChannel.writer(new FileSegmenter(mmf, 1, 1), SuiteListenerEncoding::new);
            smallEventsForSegmentSizeConcurrencyTesting(sendTo(writer), 10000000);
            writer.close();
        };
        Runnable consumer = () -> {
            IpcReader<SuiteListener> reader = IpcChannel.reader(new FileSegmenter(mmf, 2, 2), SuiteListenerEncoding::new);
            TestUtil.decodeAll(reader, expectations.getListener());
        };
        runConcurrently(producer, consumer);

        expectations.verify();
        try (DirectoryStream<Path> segments = Files.newDirectoryStream(tempDir.getRoot().toPath())) {
            for (Path segment : segments) {
                if (segment.equals(mmf)) {
                    // XXX: ignoring the first segment
                    // We can't make the consumer to wait on the producer without it opening
                    // at least one segment, so the producer may decide the size for the first segment.
                    // So we'll need to take care of it at a higher level, that the consumer won't know
                    // the file name before the producer has had time to create it.
                    continue;
                }

                assertThat("size of " + segment, Files.size(segment), is(1L));
            }
        }
    }

    private static void smallEventsForSegmentSizeConcurrencyTesting(SuiteListener listener, int nanosToPark) {
        for (int i = 0; i < 10; i++) {

            // Not a realistic scenario, because we are only interested in concurrency testing
            // the IPC protocol and not the specifics of a particular interface.
            listener.onSuiteStarted();
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
        protocol.close();
        return buffer;
    }

    private static void tryToDecode(IpcBuffer buffer) {
        buffer.position(0);
        IpcProtocol<SuiteListener> protocol = newIpcProtocol(buffer);
        TestUtil.decodeAll(protocol, mock(SuiteListener.class));
    }

    private static IpcProtocol<SuiteListener> newIpcProtocol(IpcBuffer buffer) {
        return new IpcProtocol<>(buffer, SuiteListenerEncoding::new);
    }

    private static SuiteListener sendTo(MessageSender<Event<SuiteListener>> target) {
        return new SuiteListenerEventizer().newFrontend(target);
    }
}
