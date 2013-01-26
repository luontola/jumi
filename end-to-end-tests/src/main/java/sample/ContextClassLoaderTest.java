// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package sample;

import fi.jumi.api.RunVia;
import fi.jumi.simpleunit.SimpleUnit;

@RunVia(SimpleUnit.class)
@SuppressWarnings({"UnusedDeclaration"})
public class ContextClassLoaderTest {

    public void testContextClassLoader() {
        Thread currentThread = Thread.currentThread();
        System.out.println("Current thread is " + currentThread.getName());

        ClassLoader contextClassLoader = currentThread.getContextClassLoader();
        ClassLoader currentClassLoader = getClass().getClassLoader();
        if (contextClassLoader != currentClassLoader) {
            throw new AssertionError("Expected to be the same, but were not: " +
                    "context class loader = " + contextClassLoader + ", " +
                    "current class loader = " + currentClassLoader);
        }
    }
}
