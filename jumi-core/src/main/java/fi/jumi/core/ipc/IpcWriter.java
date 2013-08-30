// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import fi.jumi.actors.eventizers.Event;
import fi.jumi.actors.queue.MessageSender;

import java.io.Closeable;

public interface IpcWriter<T> extends MessageSender<Event<T>>, Closeable {

    @Override
    void send(Event<T> message);

    @Override
    void close();
}
