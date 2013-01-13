// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.drivers;

import fi.jumi.api.drivers.Driver;
import org.apache.commons.io.IOUtils;

import javax.annotation.concurrent.*;
import java.io.*;

@NotThreadSafe
public class JUnitCompatibilityDriverFinder implements DriverFinder {

    private final Class<?> JUNIT_3_TEST;

    private final Class<?> driverClass;

    public JUnitCompatibilityDriverFinder(ClassLoader classLoader) throws ClassNotFoundException {
        JUNIT_3_TEST = classLoader.loadClass("junit.framework.Test");
        driverClass = new PackageNonDelegatingClassLoader("fi.jumi.core.junit.", classLoader)
                .loadClass("fi.jumi.core.junit.JUnitCompatibilityDriver");
    }

    @Override
    public Driver findTestClassDriver(Class<?> testClass) {
        if (JUNIT_3_TEST.isAssignableFrom(testClass)) {
            try {
                return (Driver) driverClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            return DRIVER_NOT_FOUND;
        }
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
