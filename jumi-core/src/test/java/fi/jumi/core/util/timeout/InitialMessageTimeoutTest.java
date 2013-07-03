// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.util.timeout;

import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.eventizers.dynamic.*;
import fi.jumi.actors.queue.*;
import fi.jumi.core.util.SpyListener;
import org.junit.*;

import java.util.concurrent.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;

public class InitialMessageTimeoutTest {

    private static final long NEVER = 10 * 1000;
    private static final DynamicEventizer<DummyListener> EVENTIZER = new DynamicEventizer<>(DummyListener.class);

    @Rule
    public final org.junit.rules.Timeout testTimeout = new org.junit.rules.Timeout(1000);

    private final SpyListener<DummyListener> spy = new SpyListener<>(DummyListener.class);
    private final DummyListener expect = spy.getListener();

    private final MessageQueue<Event<DummyListener>> target = new MessageQueue<>();

    @Test
    public void if_the_first_event_happens_before_timeout_then_forwards_all_messages_to_the_target_listener() {
        MessageReceiver<Event<DummyListener>> timeoutMessages = onBar123();
        InitialMessageTimeout<Event<DummyListener>> timeout = new InitialMessageTimeout<>(target, timeoutMessages, NEVER, TimeUnit.MILLISECONDS);

        sendTo(timeout).onFoo(1);
        timeout.timedOut();
        sendTo(timeout).onFoo(2);
        sendTo(timeout).onFoo(3);

        expect.onFoo(1);
        expect.onFoo(2);
        expect.onFoo(3);
        verifyExpected(target);
    }

    @Test
    public void if_times_out_before_initial_message_then_sends_timeout_messages_and_drops_all_future_messages() {
        MessageReceiver<Event<DummyListener>> timeoutMessages = onBar123();
        InitialMessageTimeout<Event<DummyListener>> timeout = new InitialMessageTimeout<>(target, timeoutMessages, NEVER, TimeUnit.MILLISECONDS);

        timeout.timedOut();
        sendTo(timeout).onFoo(1);
        sendTo(timeout).onFoo(2);
        sendTo(timeout).onFoo(3);

        expect.onBar(1);
        expect.onBar(2);
        expect.onBar(3);
        verifyExpected(target);
    }

    @Test
    public void the_timeout_happens_after_the_specified_time() throws InterruptedException {
        MessageQueue<Event<DummyListener>> target = new MessageQueue<>();
        MessageReceiver<Event<DummyListener>> timeoutMessages = onBar123();

        new InitialMessageTimeout<>(target, timeoutMessages, 0, TimeUnit.MILLISECONDS);

        assertNotNull(target.take());
    }

    @Test
    public void is_thread_safe() throws InterruptedException {
        final int ITERATIONS = 10;
        for (int i = 0; i < ITERATIONS; i++) {
            SpyDummyListener target = new SpyDummyListener();

            MessageReceiver<Event<DummyListener>> timeoutMessages = onBar123();
            InitialMessageTimeout<Event<DummyListener>> timeout = new InitialMessageTimeout<>(new EventToDynamicListener<DummyListener>(target), timeoutMessages, 0, TimeUnit.MILLISECONDS);
            sendTo(timeout).onFoo(1);
            sendTo(timeout).onFoo(2);
            sendTo(timeout).onFoo(3);

            String[] notTimedOutEvents = {"foo1", "foo2", "foo3"};
            String[] timedOutEvents = {"bar1", "bar2", "bar3"};
            String[] actual = {target.received.take(), target.received.take(), target.received.take()};
            assertThat(actual, either(arrayContaining(notTimedOutEvents)).or(arrayContaining(timedOutEvents)));
            assertThat(target.received, is(empty()));
        }
    }


    // helpers

    private static MessageReceiver<Event<DummyListener>> onBar123() {
        MessageQueue<Event<DummyListener>> messages = new MessageQueue<>();
        sendTo(messages).onBar(1);
        sendTo(messages).onBar(2);
        sendTo(messages).onBar(3);
        return messages;
    }

    private static DummyListener sendTo(MessageSender<Event<DummyListener>> timeout) {
        return EVENTIZER.newFrontend(timeout);
    }

    private void verifyExpected(MessageReceiver<Event<DummyListener>> messages) {
        spy.replay();
        Event<DummyListener> message;
        while ((message = messages.poll()) != null) {
            message.fireOn(expect);
        }
        spy.verify();
    }

    public interface DummyListener {

        void onFoo(int i);

        void onBar(int i);
    }

    private static class SpyDummyListener implements DummyListener {
        public final BlockingQueue<String> received = new ArrayBlockingQueue<>(10);

        @Override
        public void onFoo(int i) {
            received.add("foo" + i);
        }

        @Override
        public void onBar(int i) {
            received.add("bar" + i);
        }
    }
}
