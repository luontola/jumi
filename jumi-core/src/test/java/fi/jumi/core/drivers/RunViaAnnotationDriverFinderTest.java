// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.drivers;

import fi.jumi.api.RunVia;
import fi.jumi.api.drivers.*;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.util.concurrent.Executor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class RunViaAnnotationDriverFinderTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private final RunViaAnnotationDriverFinder driverFinder = new RunViaAnnotationDriverFinder();

    @Test
    public void instantiates_the_driver_specified_in_the_RunVia_annotation() {
        Driver driver = driverFinder.findTestClassDriver(DummyTestClass.class);

        assertThat(driver, is(instanceOf(DummyDriver.class)));
    }

    @Test
    public void the_RunVia_annotation_is_inherited_from_super_class() {
        Driver driver = driverFinder.findTestClassDriver(InheritsAnnotationFromSuperClass.class);

        assertThat(driver, is(instanceOf(DummyDriver.class)));
    }

    @Test
    public void does_not_support_classes_that_are_not_annotated_with_RunVia() {
        Driver driver = driverFinder.findTestClassDriver(NotAnnotatedClass.class);

        assertThat(driver, is(DriverFinder.DRIVER_NOT_FOUND));
    }


    @RunVia(DummyDriver.class)
    private static class DummyTestClass {
    }

    private static class InheritsAnnotationFromSuperClass extends DummyTestClass {
    }

    private static class NotAnnotatedClass {
    }

    static class DummyDriver extends Driver {
        @Override
        public void findTests(Class<?> testClass, SuiteNotifier notifier, Executor executor) {
        }
    }
}
