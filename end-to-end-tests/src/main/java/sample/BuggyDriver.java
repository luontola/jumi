// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package sample;

import fi.jumi.api.drivers.*;

import java.util.concurrent.Executor;

public class BuggyDriver extends Driver {

    @Override
    public void findTests(Class<?> testClass, SuiteNotifier notifier, Executor executor) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                throw new RuntimeException("dummy exception from test thread");
            }
        });
        throw new RuntimeException("dummy exception from driver thread");
    }
}
