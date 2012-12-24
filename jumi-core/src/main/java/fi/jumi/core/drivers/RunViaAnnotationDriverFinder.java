// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.drivers;

import fi.jumi.api.RunVia;
import fi.jumi.api.drivers.Driver;

import javax.annotation.concurrent.NotThreadSafe;
import java.lang.annotation.Annotation;

@NotThreadSafe
public class RunViaAnnotationDriverFinder implements DriverFinder {

    @Override
    public Driver findTestClassDriver(Class<?> testClass) {
        Class<? extends Driver> driverClass = getRequiredAnnotation(testClass, RunVia.class).value();
        try {
            return driverClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("unable to instantiate " + driverClass, e);
        }
    }

    private static <T extends Annotation> T getRequiredAnnotation(Class<?> clazz, Class<T> annotationClass) {
        T annotation = clazz.getAnnotation(annotationClass);
        if (annotation == null) {
            throw new IllegalArgumentException(clazz + " was not annotated with " + annotationClass.getName());
        }
        return annotation;
    }
}
