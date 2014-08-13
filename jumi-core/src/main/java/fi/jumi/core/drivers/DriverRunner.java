// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.drivers;

import fi.jumi.api.drivers.*;
import fi.jumi.core.util.Boilerplate;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.Executor;

@ThreadSafe
public class DriverRunner implements Runnable {

    private final Class<?> testClass;
    private final Driver driver;
    private final SuiteNotifier suiteNotifier;
    private final Executor executor;

    public DriverRunner(Driver driver, Class<?> testClass, SuiteNotifier suiteNotifier, Executor executor) {
        this.testClass = testClass;
        this.driver = driver;
        this.suiteNotifier = suiteNotifier;
        this.executor = executor;
    }

    @Override
    public void run() {
        driver.findTests(testClass, suiteNotifier, executor);
    }

    @Override
    public String toString() {
        return Boilerplate.toString(getClass(), driver, testClass);
    }
}
