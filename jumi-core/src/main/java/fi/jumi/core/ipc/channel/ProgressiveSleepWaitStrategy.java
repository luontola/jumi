// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc.channel;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class ProgressiveSleepWaitStrategy implements WaitStrategy {

    private final int yieldCount;
    private final int initialSleepMillis;
    private final int maxSleepMillis;

    private int invocations = 0;

    public ProgressiveSleepWaitStrategy() {
        this(10, 1, 10);
    }

    public ProgressiveSleepWaitStrategy(int yieldCount, int initialSleepMillis, int maxSleepMillis) {
        this.yieldCount = yieldCount;
        this.initialSleepMillis = initialSleepMillis;
        this.maxSleepMillis = maxSleepMillis;
    }

    @Override
    public void reset() {
        invocations = 0;
    }

    @Override
    public void await() {
        if (invocations < yieldCount) {
            yield();
            invocations++;
        } else {
            int millis = invocations - yieldCount + initialSleepMillis;
            sleep(millis);
            if (millis < maxSleepMillis) {
                invocations++;
            }
        }
    }


    // package-private for testing

    void yield() {
        Thread.yield();
    }

    void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
