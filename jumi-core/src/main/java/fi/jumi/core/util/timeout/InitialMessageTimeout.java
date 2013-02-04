// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.util.timeout;

import fi.jumi.actors.queue.*;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.TimeUnit;

@ThreadSafe
public class InitialMessageTimeout<T> implements MessageSender<T> {

    private final MessageSender<T> target;
    private final MessageReceiver<T> timeoutMessages;
    private final Timeout timeoutTimer;

    private State state = State.NO_MESSAGES_YET;

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
        if (state == State.TIMED_OUT) {
            return;
        }
        if (state == State.NO_MESSAGES_YET) {
            state = State.GOT_INITIAL_MESSAGE;
            timeoutTimer.cancel(); // not really needed, but disposes of the timer thread a bit sooner
        }
        target.send(message);
    }

    public void timedOut() {
        if (state == State.GOT_INITIAL_MESSAGE) {
            return;
        }
        if (state == State.NO_MESSAGES_YET) {
            state = State.TIMED_OUT;
        }
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
