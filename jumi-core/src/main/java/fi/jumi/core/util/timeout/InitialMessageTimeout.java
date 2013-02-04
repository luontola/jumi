// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.util.timeout;

import fi.jumi.actors.queue.*;

import javax.annotation.concurrent.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@ThreadSafe
public class InitialMessageTimeout<T> implements MessageSender<T> {

    private final MessageSender<T> target;
    private final MessageReceiver<T> timeoutMessages;
    private final Timeout timeoutTimer;

    private final AtomicReference<State> state = new AtomicReference<>(State.NO_MESSAGES_YET);

    @Immutable
    private enum State {
        NO_MESSAGES_YET, TIMED_OUT, GOT_INITIAL_MESSAGE
    }

    public InitialMessageTimeout(MessageSender<T> target, MessageReceiver<T> timeoutMessages, long timeout, TimeUnit unit) {
        this.target = target;
        this.timeoutMessages = timeoutMessages;
        this.timeoutTimer = new CommandExecutingTimeout(new TimedOut(), timeout, unit);
        this.timeoutTimer.start();
    }

    @Override
    public void send(T message) {
        State prev;
        do {
            prev = state.get();
            if (prev == State.TIMED_OUT) {
                return;
            }
        } while (prev != State.GOT_INITIAL_MESSAGE && !state.compareAndSet(prev, State.GOT_INITIAL_MESSAGE));

        if (prev == State.NO_MESSAGES_YET) {
            timeoutTimer.cancel(); // not really needed, but disposes of the timer thread a bit sooner
        }
        target.send(message);
    }

    public void timedOut() {
        State prev;
        do {
            prev = state.get();
            if (prev != State.NO_MESSAGES_YET) {
                return;
            }
        } while (!state.compareAndSet(prev, State.TIMED_OUT));

        copy(timeoutMessages, target);
    }

    private static <T> void copy(MessageReceiver<T> source, MessageSender<T> target) {
        T message;
        while ((message = source.poll()) != null) {
            target.send(message);
        }
    }


    @ThreadSafe
    private class TimedOut implements Runnable {
        @Override
        public void run() {
            timedOut();
        }
    }
}
