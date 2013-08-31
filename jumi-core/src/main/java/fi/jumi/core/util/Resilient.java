// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.util;

import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;

@ThreadSafe
public class Resilient {

    public static <T> T tryRepeatedly(IoAction<T> action) throws IOException {
        return tryRepeatedly(10, action);
    }

    public static <T> T tryRepeatedly(int maxTries, IoAction<T> action) throws IOException {
        for (int tries = 1; ; tries++) {
            try {
                return action.run();
            } catch (IOException e) {
                if (tries >= maxTries) {
                    throw e;
                } else {
                    sleep(10);
                }
            }
        }
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    public interface IoAction<T> {
        T run() throws IOException;
    }
}
