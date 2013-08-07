// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.suite;

import fi.jumi.api.drivers.*;
import fi.jumi.core.api.*;
import fi.jumi.core.drivers.DriverFinder;
import fi.jumi.core.util.MethodCallSpy;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.Executor;

import static fi.jumi.core.util.JumiMatchers.stackTrace;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class SuiteRunnerTest extends SuiteRunnerIntegrationHelper {

    private static final Class<?> CLASS_1 = DummyTest.class;
    private static final Class<?> CLASS_2 = SecondDummyTest.class;

    @Test
    public void runs_all_test_classes_which_are_found() {
        Driver anyDriver = mock(Driver.class);

        run(anyDriver, CLASS_1, CLASS_2);

        assertRunsTestClass(CLASS_1, anyDriver);
        assertRunsTestClass(CLASS_2, anyDriver);
    }

    @Test
    public void runs_each_test_class_using_its_own_driver() {
        Driver driverForClass1 = mock(Driver.class, "driver1");
        Driver driverForClass2 = mock(Driver.class, "driver2");

        run(new FakeDriverFinder()
                .map(CLASS_1, driverForClass1)
                .map(CLASS_2, driverForClass2), CLASS_1, CLASS_2);

        assertRunsTestClass(CLASS_1, driverForClass1);
        assertRunsTestClass(CLASS_2, driverForClass2);
    }

    private static void assertRunsTestClass(Class<?> testClass, Driver driverForAllClasses) {
        verify(driverForAllClasses).findTests(eq(testClass), any(SuiteNotifier.class), any(Executor.class));
    }

    @Test
    public void removes_duplicate_onTestFound_events() {
        SuiteListener listener = mock(SuiteListener.class);

        run(listener, new Driver() {
            @Override
            public void findTests(Class<?> testClass, SuiteNotifier notifier, Executor executor) {
                notifier.fireTestFound(TestId.ROOT, "test name");
                notifier.fireTestFound(TestId.ROOT, "test name");
            }
        }, CLASS_1);

        verify(listener, times(1)).onTestFound(TestFile.fromClass(CLASS_1), TestId.ROOT, "test name");
    }

    @Test
    public void notifies_when_all_test_classes_are_finished() {
        MethodCallSpy spy = new MethodCallSpy();
        SuiteListener listener = spy.createProxyTo(SuiteListener.class);

        run(listener, new FakeTestClassDriver(), CLASS_1, CLASS_2);

        assertThat("should happen once", spy.countCallsTo("onSuiteFinished"), is(1));
        assertThat("should happen last", spy.getLastCall(), is("onSuiteFinished"));
    }

    @Test
    public void reports_uncaught_exceptions_from_driver_threads_as_internal_errors() {
        SuiteListener listener = mock(SuiteListener.class);

        run(listener, new Driver() {
            @Override
            public void findTests(Class<?> testClass, SuiteNotifier notifier, Executor executor) {
                throw new RuntimeException("dummy exception from driver thread");
            }
        }, CLASS_1);

        verify(listener).onInternalError(
                eq("Uncaught exception in thread " + Thread.currentThread().getName()),
                stackTrace("java.lang.RuntimeException: dummy exception from driver thread"));
    }

    @Test
    public void reports_uncaught_exceptions_from_test_threads_as_internal_errors() {
        SuiteListener listener = mock(SuiteListener.class);

        run(listener, new Driver() {
            @Override
            public void findTests(Class<?> testClass, SuiteNotifier notifier, Executor executor) {
                executor.execute(() -> {
                    throw new RuntimeException("dummy exception from test thread");
                });
            }
        }, CLASS_1);

        verify(listener).onInternalError(
                eq("Uncaught exception in thread " + Thread.currentThread().getName()),
                stackTrace("java.lang.RuntimeException: dummy exception from test thread"));
    }


    // guinea pigs

    private static class DummyTest {
    }

    private static class SecondDummyTest {
    }

    public static class FakeTestClassDriver extends Driver {
        @Override
        public void findTests(Class<?> testClass, SuiteNotifier notifier, Executor executor) {
            notifier.fireTestFound(TestId.ROOT, testClass.getSimpleName());
            notifier.fireTestStarted(TestId.ROOT)
                    .fireTestFinished();
        }
    }

    private static class FakeDriverFinder implements DriverFinder {
        private final Map<Class<?>, Driver> driverMapping = new HashMap<>();

        @Override
        public Driver findTestClassDriver(Class<?> testClass) {
            Driver driver = driverMapping.get(testClass);
            if (driver == null) {
                throw new IllegalArgumentException("unexpected class: " + testClass);
            }
            return driver;
        }

        public FakeDriverFinder map(Class<?> testClass, Driver driver) {
            driverMapping.put(testClass, driver);
            return this;
        }
    }
}
