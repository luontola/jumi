// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.drivers;

import fi.jumi.api.drivers.Driver;
import fi.jumi.core.junit.LegacyJUnitDriver;
import junit.framework.Test;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class JUnitDriverFinder implements DriverFinder {

    private static final Class<Test> JUNIT_3_TEST = Test.class;

    @Override
    public Driver findTestClassDriver(Class<?> testClass) {
        if (JUNIT_3_TEST.isAssignableFrom(testClass)) {
            return new LegacyJUnitDriver();
        } else {
            return DRIVER_NOT_FOUND;
        }
    }
}
