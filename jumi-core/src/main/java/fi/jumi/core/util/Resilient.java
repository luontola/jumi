// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.util;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class Resilient {

    public static <T, E extends Throwable> T tryRepeatedly(Action<T, E> action) throws E {
        return tryRepeatedly(10, action);
    }

    public static <T, E extends Throwable> T tryRepeatedly(int maxTries, Action<T, E> action) throws E {
        for (int tries = 1; ; tries++) {
            try {
                return action.run();
            } catch (Exception e) {
                if (tries >= maxTries) {
                    throw e;
                } else {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e1) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    public interface Action<T, E extends Throwable> {
        T run() throws E;
    }
}
