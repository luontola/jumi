// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.drivers;

import fi.jumi.api.RunVia;
import fi.jumi.api.drivers.*;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.concurrent.Executor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class DriverFinderFactoryTest {

    @Test
    public void Jumi_has_higher_priority_than_JUnit() {
        CompositeDriverFinder finder = DriverFinderFactory.createDriverFinder();

        Driver driver = finder.findTestClassDriver(EveryPossibleFrameworkTest.class);

        assertThat(driver, is(instanceOf(DummyJumiDriver.class)));
    }


    @RunVia(DummyJumiDriver.class)
    @RunWith(Parameterized.class)
    @SuppressWarnings("JUnit4AnnotatedMethodInJUnit3TestCase")
    private static class EveryPossibleFrameworkTest extends TestCase {

        @Test
        public void testFoo() {
        }
    }

    static class DummyJumiDriver extends Driver {
        @Override
        public void findTests(Class<?> testClass, SuiteNotifier notifier, Executor executor) {
        }
    }
}
