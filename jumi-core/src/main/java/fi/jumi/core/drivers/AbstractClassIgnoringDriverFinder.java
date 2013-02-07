// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.drivers;

import fi.jumi.api.drivers.Driver;

import javax.annotation.concurrent.Immutable;
import java.lang.reflect.Modifier;

@Immutable
public class AbstractClassIgnoringDriverFinder implements DriverFinder {

    @Override
    public Driver findTestClassDriver(Class<?> testClass) {
        if (Modifier.isAbstract(testClass.getModifiers())) {
            return new IgnoreSilentlyDriver();
        }
        return DRIVER_NOT_FOUND;
    }
}
