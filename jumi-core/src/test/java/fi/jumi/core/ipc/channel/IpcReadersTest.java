// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc.channel;

import org.junit.*;
import org.junit.rules.ExpectedException;

public class IpcReadersTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void on_interrupt_throws_InterruptedException() throws Exception {
        IpcReader<Consumer<String>> reader = target -> PollResult.NO_NEW_MESSAGES;
        Consumer<String> target = s -> {
        };

        Thread.currentThread().interrupt();

        thrown.expect(InterruptedException.class);
        IpcReaders.decodeAll(reader, target);
    }

    private interface Consumer<T> {
        void accept(T t);
    }
}
