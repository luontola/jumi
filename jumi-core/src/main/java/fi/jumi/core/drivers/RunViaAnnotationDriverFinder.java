// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.drivers;

import fi.jumi.api.RunVia;
import fi.jumi.api.drivers.Driver;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class RunViaAnnotationDriverFinder implements DriverFinder {

    @Override
    public Driver findTestClassDriver(Class<?> testClass) {
        RunVia annotation = testClass.getAnnotation(RunVia.class);
        if (annotation == null) {
            return DRIVER_NOT_FOUND;
        }
        Class<? extends Driver> driverClass = annotation.value();
        try {
            return driverClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("unable to instantiate " + driverClass, e);
        }
    }
}
