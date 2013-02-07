// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.drivers;

import fi.jumi.api.RunVia;
import fi.jumi.api.drivers.*;
import fi.jumi.core.junit.JUnitCompatibilityDriver;
import fi.jumi.core.util.BlacklistClassLoader;
import junit.framework.TestCase;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.*;
import java.util.concurrent.Executor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class DriverFinderFactoryTest {

    private final ByteArrayOutputStream logOutput = new ByteArrayOutputStream();
    private final CompositeDriverFinder finder = DriverFinderFactory.createDriverFinder(getClass().getClassLoader(), new PrintStream(logOutput));

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
    public void does_not_support_JUnit_tests_if_JUnit_not_on_test_classpath() {
        BlacklistClassLoader noJUnitInThisClassLoader = new BlacklistClassLoader("org.junit.", getClass().getClassLoader());
        CompositeDriverFinder finder = DriverFinderFactory.createDriverFinder(noJUnitInThisClassLoader, new PrintStream(logOutput));

        Driver driver = finder.findTestClassDriver(JUnitTest.class);

        assertThat(driver, is(instanceOf(IgnoreSilentlyDriver.class)));
        assertThat(logOutput.toString(), containsString("JUnit not found on classpath; disabling JUnit compatibility"));
    }

    @Test
    public void Jumi_drivers_have_priority_over_all_other_testing_frameworks() {
        Driver driver = finder.findTestClassDriver(EveryPossibleFrameworkTest.class);

        assertThat(driver, is(instanceOf(DummyJumiDriver.class)));
    }


    // ignored class categories

    @Test
    public void silently_ignores_abstract_classes() {
        Driver driver = finder.findTestClassDriver(AbstractTest.class);

        assertThat(driver, is(instanceOf(IgnoreSilentlyDriver.class)));
    }

    @Test
    public void silently_ignores_interfaces() {
        Driver driver = finder.findTestClassDriver(InterfaceTest.class);

        assertThat(driver, is(instanceOf(IgnoreSilentlyDriver.class)));
    }

    @Test
    public void ignores_non_test_classes_with_a_warning_in_the_daemon_logs() {
        Driver driver = finder.findTestClassDriver(NotReallyTest.class);

        assertThat(driver, is(instanceOf(IgnoreSilentlyDriver.class)));
        // We don't want to nag the users indefinitely, but may be a good idea to have some way of knowing these:
        assertThat(logOutput.toString(), containsString("Not recognized as a test class: fi.jumi.core.drivers.DriverFinderFactoryTest$NotReallyTest"));
    }


    // guinea pigs

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

    @RunVia(DummyJumiDriver.class)
    private static abstract class AbstractTest {
    }

    @RunVia(DummyJumiDriver.class)
    private interface InterfaceTest {
    }

    private static class NotReallyTest {
    }

    static class DummyJumiDriver extends Driver {
        @Override
        public void findTests(Class<?> testClass, SuiteNotifier notifier, Executor executor) {
        }
    }
}
