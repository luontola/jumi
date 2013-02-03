// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.suite;

import fi.jumi.api.drivers.Driver;
import fi.jumi.core.api.SuiteListener;
import fi.jumi.core.drivers.DriverFinder;
import fi.jumi.core.testbench.*;
import fi.jumi.core.util.SpyListener;

import java.io.PrintStream;

public abstract class SuiteRunnerIntegrationHelper {

    private final SpyListener<SuiteListener> spy = new SpyListener<>(SuiteListener.class);
    protected final SuiteListener expect = spy.getListener();

    private final TestBench testBench = new TestBench();
    protected final PrintStream stdout = testBench.out;
    protected final PrintStream stderr = testBench.err;

    protected void runAndCheckExpectations(Driver driver, Class<?>... testClasses) {
        spy.replay();
        run(driver, testClasses);
        spy.verify();
    }

    protected void run(Driver driver, Class<?>... testClasses) {
        run(new StubDriverFinder(driver), testClasses);
    }

    protected void run(DriverFinder driverFinder, Class<?>... testClasses) {
        run(expect, driverFinder, testClasses);
    }

    protected void run(SuiteListener suiteListener, Driver driver, Class<?>... testClasses) {
        run(suiteListener, new StubDriverFinder(driver), testClasses);
    }

    protected void run(SuiteListener suiteListener, DriverFinder driverFinder, Class<?>... testClasses) {
        testBench.setDriverFinder(driverFinder);
        testBench.run(suiteListener, testClasses);
    }
}
