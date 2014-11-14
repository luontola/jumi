// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.util;

import com.google.common.base.Throwables;
import org.junit.Test;
import org.junit.internal.runners.statements.Fail;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class TestableRandomTest {

    @Test
    public void on_failure_shows_the_original_exception_message() {
        TestableRandom random = new TestableRandom(123);

        Throwable e = getExceptionThrownOnTestFailure(random);

        assertThat(Throwables.getStackTraceAsString(e), containsString("original message"));
    }

    @Test
    public void on_failure_shows_the_initial_seed() {
        TestableRandom random = new TestableRandom(123);

        Throwable e = getExceptionThrownOnTestFailure(random);

        assertThat(Throwables.getStackTraceAsString(e), containsString("seed was 123L"));
    }

    @Test
    public void on_failure_shows_each_generated_random_number() {
        TestableRandom random = new TestableRandom(123);

        random.nextInt();
        random.nextShort();

        Throwable e = getExceptionThrownOnTestFailure(random);
        assertThat(Throwables.getStackTraceAsString(e), containsString("" +
                "- (int) -1188957731\n" +
                "- (short) 1173"));
    }

    @Test
    public void on_failures_shows_when_seed_was_reset_and_all_events_before_it() {
        TestableRandom random = new TestableRandom(123);

        random.nextInt();
        random.resetSeed();
        random.nextInt();

        Throwable e = getExceptionThrownOnTestFailure(random);
        assertThat(Throwables.getStackTraceAsString(e), containsString("" +
                "- (int) -1188957731\n" +
                "- reset seed\n" +
                "- (int) -1188957731"));
    }


    private static Throwable getExceptionThrownOnTestFailure(TestableRandom random) {
        try {
            random.apply(new Fail(new Exception("original message")), null).evaluate();
        } catch (Throwable t) {
            return t;
        }
        throw new AssertionError("expected to throw exception, but did not");
    }
}
