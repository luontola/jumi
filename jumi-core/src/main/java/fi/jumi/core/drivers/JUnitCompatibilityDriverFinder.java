// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.drivers;

import fi.jumi.api.drivers.Driver;
import org.apache.commons.io.IOUtils;

import javax.annotation.concurrent.*;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@NotThreadSafe
public class JUnitCompatibilityDriverFinder implements DriverFinder {

    private final Class<? extends Annotation> JUNIT_4_TEST;
    private final Class<?> JUNIT_3_TEST;

    private final Class<? extends Driver> driverClass;

    @SuppressWarnings("unchecked")
    public JUnitCompatibilityDriverFinder(ClassLoader classLoader) throws ClassNotFoundException {
        JUNIT_4_TEST = (Class<? extends Annotation>) classLoader.loadClass("org.junit.Test");
        JUNIT_3_TEST = classLoader.loadClass("junit.framework.Test");
        driverClass = (Class<? extends Driver>)
                new PackageNonDelegatingClassLoader("fi.jumi.core.junit.", classLoader)
                        .loadClass("fi.jumi.core.junit.JUnitCompatibilityDriver");
    }

    @Override
    public Driver findTestClassDriver(Class<?> testClass) {
        if (isJUnit4Test(testClass) || isJUnit3Test(testClass)) {
            try {
                return driverClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            return DRIVER_NOT_FOUND;
        }
    }

    private boolean isJUnit4Test(Class<?> testClass) {
        for (Method method : testClass.getMethods()) {
            if (method.getAnnotation(JUNIT_4_TEST) != null) {
                return true;
            }
        }
        return false;
    }

    private boolean isJUnit3Test(Class<?> testClass) {
        return JUNIT_3_TEST.isAssignableFrom(testClass);
    }

    @ThreadSafe
    private static class PackageNonDelegatingClassLoader extends ClassLoader {
        private final String classNamePrefix;

        public PackageNonDelegatingClassLoader(String classNamePrefix, ClassLoader parent) {
            super(parent);
            this.classNamePrefix = classNamePrefix;
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            if (name.startsWith(classNamePrefix)) {
                byte[] bytes = getBytecode(name);
                if (bytes != null) {
                    defineClass(name, bytes, 0, bytes.length);
                }
            }
            return super.loadClass(name);
        }

        private byte[] getBytecode(String name) {
            String resource = name.replace('.', '/') + ".class";
            InputStream in = getResourceAsStream(resource);
            if (in == null) {
                return null;
            }
            try {
                return IOUtils.toByteArray(in);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                IOUtils.closeQuietly(in);
            }
        }
    }
}
