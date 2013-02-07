// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.drivers;

import fi.jumi.core.util.LocallyDefiningClassLoader;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;

@NotThreadSafe
public class DriverFinderFactory {

    public static CompositeDriverFinder createDriverFinder(ClassLoader testClassLoader) {
        List<DriverFinder> driverFinders = new ArrayList<>();
        driverFinders.add(new AbstractClassIgnoringDriverFinder());
        driverFinders.add(new RunViaAnnotationDriverFinder());
        try {
            driverFinders.add((DriverFinder)
                    new LocallyDefiningClassLoader("fi.jumi.core.junit.", testClassLoader)
                            .loadClass("fi.jumi.core.junit.JUnitCompatibilityDriverFinder")
                            .newInstance());
        } catch (Exception e) {
            // JUnit not on classpath; ignore
            System.out.println("JUnit not found on classpath; disabling JUnit compatibility");
        }
        return new CompositeDriverFinder(driverFinders);
    }
}
