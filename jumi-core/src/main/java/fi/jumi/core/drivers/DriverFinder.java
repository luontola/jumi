// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.drivers;

import fi.jumi.api.drivers.Driver;

public interface DriverFinder {

    Driver DRIVER_NOT_FOUND = null;

    /**
     * Returns the {@link Driver} for running {@code testClass}, or {@link #DRIVER_NOT_FOUND} if this {@code
     * DriverFinder} does not know the {@link Driver} for the specified {@code testClass}.
     */
    Driver findTestClassDriver(Class<?> testClass);
}
