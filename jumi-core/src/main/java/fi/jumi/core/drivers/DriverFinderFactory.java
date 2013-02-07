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
        try {
            LocallyDefiningClassLoader classLoader = new LocallyDefiningClassLoader("fi.jumi.core.junit.", testClassLoader);
            classLoader.loadClass("org.junit.Test");
            driverFinders.add((DriverFinder) classLoader.loadClass("fi.jumi.core.junit.JUnitCompatibilityDriverFinder").newInstance());
        } catch (Exception e) {
            // JUnit not on classpath; ignore
            System.out.println("JUnit not found on classpath; disabling JUnit compatibility"); // TODO: test me
        }
        driverFinders.add(new NonTestClassesIgnoringDriverFinder(logOutput));
        return new CompositeDriverFinder(driverFinders);
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
