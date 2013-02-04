// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.util.timeout;

import fi.jumi.actors.queue.*;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class InitialMessageTimeout<T> implements MessageSender<T> {

    private enum State {
        NO_MESSAGES_YET, TIMED_OUT, GOT_INITIAL_MESSAGE
    }

    private final MessageSender<T> target;
    private final MessageReceiver<T> timeoutMessages;
    private State state = State.NO_MESSAGES_YET;

    public InitialMessageTimeout(MessageSender<T> target, MessageReceiver<T> timeoutMessages) {
        this.target = target;
        this.timeoutMessages = timeoutMessages;
    }

    @Override
    public void send(T message) {
        if (state == State.TIMED_OUT) {
            return;
        }
        if (state == State.NO_MESSAGES_YET) {
            state = State.GOT_INITIAL_MESSAGE;
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
}
