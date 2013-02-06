// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.drivers;

import fi.jumi.api.RunVia;
import fi.jumi.api.drivers.*;
import fi.jumi.core.junit.JUnitCompatibilityDriver;
import junit.framework.TestCase;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.concurrent.Executor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class DriverFinderFactoryTest {

    private final CompositeDriverFinder finder = DriverFinderFactory.createDriverFinder(getClass().getClassLoader());

    @Test
    public void supports_RunVia_annotated_Jumi_tests() {
        @RunVia(DummyJumiDriver.class)
        class RunViaAnnotatedClass {
        }

        Driver driver = finder.findTestClassDriver(RunViaAnnotatedClass.class);

        Assert.assertThat(driver, is(instanceOf(DummyJumiDriver.class)));
    }

    @Test
    public void supports_JUnit_tests() {
        Driver driver = finder.findTestClassDriver(JUnitTest.class);

        //assertThat(driver, is(instanceOf(JUnitCompatibilityDriver.class))); // TODO: use me instead
        assertThat(driver.getClass().getName(), is(JUnitCompatibilityDriver.class.getName()));
    }

    @Test
    public void Jumi_drivers_have_the_highest_priority() {
        Driver driver = finder.findTestClassDriver(EveryPossibleFrameworkTest.class);

        assertThat(driver, is(instanceOf(DummyJumiDriver.class)));
    }


    // helpers

    @RunVia(DummyJumiDriver.class)
    @RunWith(Parameterized.class)
    @SuppressWarnings("JUnit4AnnotatedMethodInJUnit3TestCase")
    private static class EveryPossibleFrameworkTest extends TestCase {

        @Test
        public void testFoo() {
        }
    }

    private static class JUnitTest extends TestCase {
    }

    static class DummyJumiDriver extends Driver {
        @Override
        public void findTests(Class<?> testClass, SuiteNotifier notifier, Executor executor) {
        }
    }
}
