// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.junit;

import fi.jumi.api.drivers.Driver;
import fi.jumi.core.drivers.DriverFinder;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.concurrent.NotThreadSafe;
import java.lang.reflect.Method;

@NotThreadSafe
public class JUnitCompatibilityDriverFinder implements DriverFinder {

    @Override
    public Driver findTestClassDriver(Class<?> testClass) {
        if (isJUnit4AnnotatedTest(testClass) || isJUnit4Test(testClass) || isJUnit3Test(testClass)) {
            return new JUnitCompatibilityDriver();
        } else {
            return DRIVER_NOT_FOUND;
        }
    }

    private static boolean isJUnit4AnnotatedTest(Class<?> testClass) {
        return testClass.getAnnotation(RunWith.class) != null;
    }

    private static boolean isJUnit4Test(Class<?> testClass) {
        for (Method method : testClass.getMethods()) {
            if (method.getAnnotation(Test.class) != null) {
                return true;
            }
        }
        return false;
    }

    private static boolean isJUnit3Test(Class<?> testClass) {
        return TestCase.class.isAssignableFrom(testClass);
    }
}
