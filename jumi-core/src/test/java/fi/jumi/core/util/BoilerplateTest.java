// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.util;

import org.junit.*;
import org.junit.rules.ExpectedException;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
public class BoilerplateTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void rethrow_wraps_checked_exceptions_before_rethrowing_them() {
        Exception e = new IOException("foo");

        thrown.expect(RuntimeException.class);
        thrown.expectCause(equalTo(e));
        Boilerplate.rethrow(e);
    }

    @Test
    public void rethrow_rethrows_unchecked_exceptions_as_is() {
        Exception e = new IllegalArgumentException("foo");

        thrown.expect(equalTo(e));
        Boilerplate.rethrow(e);
    }

    @Test
    public void toString_shows_class_name_and_fields() {
        assertThat("no fields", Boilerplate.toString(getClass()), is("BoilerplateTest()"));
        assertThat("one field", Boilerplate.toString(getClass(), "foo"), is("BoilerplateTest(foo)"));
        assertThat("many fields", Boilerplate.toString(getClass(), "bar", 42), is("BoilerplateTest(bar, 42)"));
        assertThat("inner class", Boilerplate.toString(InnerClass.class), is("BoilerplateTest$InnerClass()"));
    }

    private static class InnerClass {
    }
}
