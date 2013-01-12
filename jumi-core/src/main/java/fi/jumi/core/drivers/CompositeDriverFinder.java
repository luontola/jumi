// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.drivers;

import fi.jumi.api.drivers.Driver;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Arrays;

@NotThreadSafe
public class CompositeDriverFinder implements DriverFinder {

    private final DriverFinder[] finders;

    public CompositeDriverFinder(DriverFinder... finders) {
        this.finders = finders;
    }

    @Override
    public Driver findTestClassDriver(Class<?> testClass) {
        for (DriverFinder finder : finders) {
            Driver driver = finder.findTestClassDriver(testClass);
            if (driver != DRIVER_NOT_FOUND) {
                return driver;
            }
        }
        throw new IllegalArgumentException(testClass + " was not recognized as a compatible test class; tried " + Arrays.toString(finders));
    }
}
