// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.drivers;

import fi.jumi.api.drivers.Driver;
import fi.jumi.core.util.LocallyDefiningClassLoader;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.PrintStream;
import java.util.*;

@NotThreadSafe
public class DriverFinderFactory {

    public static CompositeDriverFinder createDriverFinder(ClassLoader testClassLoader, PrintStream logOutput) {
        List<DriverFinder> driverFinders = new ArrayList<>();
        driverFinders.add(new AbstractClassIgnoringDriverFinder());
        driverFinders.add(new RunViaAnnotationDriverFinder());
        if (isOnClasspath("org.junit.Test", testClassLoader)) {
            driverFinders.add(createJUnitCompatibilityDriverFinder(testClassLoader));
        } else {
            logOutput.println("JUnit not found on classpath; disabling JUnit compatibility");
        }
        driverFinders.add(new NonTestClassesIgnoringDriverFinder(logOutput));
        return new CompositeDriverFinder(driverFinders);
    }

    private static DriverFinder createJUnitCompatibilityDriverFinder(ClassLoader classLoader) {
        try {
            // XXX: JUnitCompatibilityDriverFinder must be loaded from a class loader that has JUnit on its classpath,
            // but our current class loader is the Jumi daemon's class loader, and only the test class loader has JUnit.
            return (DriverFinder)
                    new LocallyDefiningClassLoader("fi.jumi.core.junit.", classLoader)
                            .loadClass("fi.jumi.core.junit.JUnitCompatibilityDriverFinder")
                            .newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isOnClasspath(String className, ClassLoader classLoader) {
        try {
            classLoader.loadClass(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }


    @NotThreadSafe
    private static class NonTestClassesIgnoringDriverFinder implements DriverFinder {
        private final PrintStream logOutput;

        public NonTestClassesIgnoringDriverFinder(PrintStream logOutput) {
            this.logOutput = logOutput;
        }

        @Override
        public Driver findTestClassDriver(Class<?> testClass) {
            logOutput.println("Not recognized as a test class: " + testClass.getName());
            return new IgnoreSilentlyDriver();
        }
    }
}
