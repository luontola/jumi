// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.serialization;

import fi.jumi.core.api.StackTrace;
import org.junit.Test;

import java.io.IOException;

import static fi.jumi.core.util.EqualityMatchers.deepEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SuiteListenerEncodingTest {

    @Test
    public void test_serialization_of_StackTrace() {
        StackTrace original = StackTrace.from(new IOException());

        assertThat(roundTripStackTrace(original), is(deepEqualTo(original)));
    }

    @Test
    public void test_serialization_of_StackTrace_with_message() {
        StackTrace original = StackTrace.from(new IOException("the message"));

        assertThat(roundTripStackTrace(original), is(deepEqualTo(original)));
    }

    @Test
    public void test_serialization_of_StackTrace_with_causes() {
        StackTrace original = StackTrace.from(
                new IOException("the message",
                        new IllegalArgumentException("cause 1",
                                new IllegalStateException("cause 2"))));

        assertThat(roundTripStackTrace(original), is(deepEqualTo(original)));
    }

    @Test
    public void test_serialization_of_StackTrace_with_suppressed() {
        IOException e = new IOException("the message");
        e.addSuppressed(new IllegalArgumentException("suppressed 1"));
        e.addSuppressed(new IllegalStateException("suppressed 2"));
        StackTrace original = StackTrace.from(e);

        assertThat(roundTripStackTrace(original), is(deepEqualTo(original)));
    }


    private static StackTrace roundTripStackTrace(StackTrace original) {
        return TestUtil.serializeAndDeserialize(original,
                (buffer, data) -> new SuiteListenerEncoding(buffer).writeStackTrace(data),
                SuiteListenerEncoding::readStackTrace);
    }
}
