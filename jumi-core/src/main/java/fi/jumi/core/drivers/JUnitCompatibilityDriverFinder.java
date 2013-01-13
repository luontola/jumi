// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.drivers;

import fi.jumi.api.drivers.Driver;
import fi.jumi.core.junit.JUnitCompatibilityDriver;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class JUnitCompatibilityDriverFinder implements DriverFinder {

    private final Class<?> JUNIT_3_TEST;

    public JUnitCompatibilityDriverFinder(ClassLoader classLoader) throws ClassNotFoundException {
        JUNIT_3_TEST = classLoader.loadClass("junit.framework.Test");
    }

    @Override
    public Driver findTestClassDriver(Class<?> testClass) {
        if (JUNIT_3_TEST.isAssignableFrom(testClass)) {
            return new JUnitCompatibilityDriver();
        } else {
            return DRIVER_NOT_FOUND;
        }
    }
}
