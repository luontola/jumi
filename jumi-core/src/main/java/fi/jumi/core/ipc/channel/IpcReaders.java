// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc.channel;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class IpcReaders {

    public static <T> void decodeAll(IpcReader<T> reader, T target) throws InterruptedException {
        WaitStrategy waitStrategy = new ProgressiveSleepWaitStrategy();
        while (true) {
            PollResult result = reader.poll(target);
            if (result == PollResult.NO_NEW_MESSAGES) {
                waitStrategy.snooze();
            }
            if (result == PollResult.HAD_SOME_MESSAGES) {
                waitStrategy.reset();
            }
            if (result == PollResult.END_OF_STREAM) {
                return;
            }
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
        }
    }
}
