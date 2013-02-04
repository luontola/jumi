// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.drivers;

import fi.jumi.api.drivers.*;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.util.concurrent.Executor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class CompositeDriverFinderTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private final Class<DummyTest> testClass = DummyTest.class;
    private final Driver expectedDriver = new DummyDriver();

    @Test
    public void returns_the_driver_from_the_backing_driver_finder() {
        DriverFinder finder1 = mock(DriverFinder.class, "finder1");
        stub(finder1.findTestClassDriver(testClass)).toReturn(expectedDriver);

        Driver driver = new CompositeDriverFinder(finder1).findTestClassDriver(testClass);

        assertThat(driver, is(expectedDriver));
    }

    @Test
    public void returns_the_driver_from_the_first_compatible_backing_driver_finder() {
        DriverFinder finder1 = mock(DriverFinder.class, "finder1");
        DriverFinder finder2 = mock(DriverFinder.class, "finder2");
        DriverFinder finder3 = mock(DriverFinder.class, "finder3");
        stub(finder1.findTestClassDriver(testClass)).toReturn(null);
        stub(finder2.findTestClassDriver(testClass)).toReturn(expectedDriver);
        stub(finder3.findTestClassDriver(testClass)).toReturn(null);

        Driver driver = new CompositeDriverFinder(finder1, finder2, finder3).findTestClassDriver(testClass);

        assertThat(driver, is(expectedDriver));
        verify(finder1).findTestClassDriver(testClass);
        verify(finder2).findTestClassDriver(testClass);
        verifyZeroInteractions(finder3);
    }

    @Test
    public void fails_if_none_of_the_backing_driver_finders_was_compatible() {
        DriverFinder finder1 = mock(DriverFinder.class, "finder1");
        stub(finder1.findTestClassDriver(testClass)).toReturn(null);

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(testClass + " was not recognized as a compatible test class");
        thrown.expectMessage(finder1.toString());
        new CompositeDriverFinder(finder1).findTestClassDriver(testClass);
    }


    private static class DummyTest {
    }

    private static class DummyDriver extends Driver {
        @Override
        public void findTests(Class<?> testClass, SuiteNotifier notifier, Executor executor) {
        }
    }
}
