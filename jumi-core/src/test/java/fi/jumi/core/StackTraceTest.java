// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core;

import com.google.common.base.Throwables;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
public class StackTraceTest {

    @Test
    public void has_same_message_as_original_exception() {
        Throwable original = new Throwable("original message");

        StackTrace copy = StackTrace.copyOf(original);

        assertThat(copy.getMessage(), is("original message"));
    }

    @Test
    public void has_same_toString_as_original_exception() {
        Throwable original = new Throwable("original message");

        StackTrace copy = StackTrace.copyOf(original);

        assertThat(copy.toString(), is(original.toString()));
    }

    @Test
    public void has_same_stack_trace_as_original_exception() {
        Throwable original = new Throwable("original message");

        StackTrace copy = StackTrace.copyOf(original);

        assertThat(copy.getStackTrace(), is(arrayContaining(original.getStackTrace())));
        assertThat(Throwables.getStackTraceAsString(copy), is(Throwables.getStackTraceAsString(original)));
    }

    @Test
    public void provides_the_name_of_the_original_exception_class() {
        Throwable original = new Throwable("original message");

        StackTrace copy = StackTrace.copyOf(original);

        assertThat(copy.getExceptionClass(), is("java.lang.Throwable"));
    }

    @Test
    public void causes_are_also_copied_recursively() {
        Throwable original = new Throwable("original message", new Exception("the cause"));

        StackTrace copy = StackTrace.copyOf(original);

        assertThat(copy.getCause(), is(instanceOf(StackTrace.class)));
        assertThat(copy.getCause().getMessage(), is("the cause"));
    }

    @Test
    public void suppressed_exceptions_are_also_copied_recursively() {
        Throwable original = new Throwable("original message");
        original.addSuppressed(new Exception("suppressed 1"));
        original.addSuppressed(new RuntimeException("suppressed 2"));

        StackTrace copy = StackTrace.copyOf(original);

        Throwable[] suppressed = copy.getSuppressed();
        assertThat(suppressed.length, is(2));
        assertThat(suppressed[0], is(instanceOf(StackTrace.class)));
        assertThat(suppressed[0].getMessage(), is("suppressed 1"));
        assertThat(suppressed[1], is(instanceOf(StackTrace.class)));
        assertThat(suppressed[1].getMessage(), is("suppressed 2"));
    }
}
