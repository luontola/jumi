// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.util;

import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

public class ResilientTest {

    @Test
    public void executes_once_if_there_is_no_exception() throws Throwable {
        String result = Resilient.tryRepeatedly(10, () -> "result");

        assertThat(result, is("result"));
    }

    @Test
    public void retries_if_there_is_a_flaky_exception() throws IOException {
        AtomicInteger invocation = new AtomicInteger(0);

        String result = Resilient.tryRepeatedly(10, () -> {
            if (invocation.incrementAndGet() == 1) {
                throw new IOException("dummy exception");
            }
            return "result";
        });

        assertThat(result, is("result"));
    }

    @Test
    public void gives_up_and_rethrows_the_exception_if_fails_too_many_times() {
        int maxTries = 3;
        AtomicInteger invocations = new AtomicInteger(0);

        try {
            Resilient.tryRepeatedly(maxTries, () -> {
                invocations.incrementAndGet();
                throw new IOException("dummy exception");
            });
            fail("should have thrown IOException");

        } catch (IOException e) {
            assertThat("exception message", e.getMessage(), is("dummy exception"));
            assertThat("invocations", invocations.get(), is(maxTries));
        }
    }
}
