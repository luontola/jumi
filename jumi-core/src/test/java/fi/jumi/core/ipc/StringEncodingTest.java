// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

public class StringEncodingTest {

    @Test
    public void test_serialization_of_String() {
        assertThat("empty string", roundTripString(""), is(""));

        String original = RandomStringUtils.random(10);
        assertThat("random string", roundTripString(original), is(original));
    }

    @Test
    public void test_serialization_of_String_with_non_printable_characters() {
        for (char c = 0; c < ' '; c++) {
            String original = "" + c;
            assertThat("0x" + Integer.toHexString(c), roundTripString(original), is(original));
        }
    }

    @Test
    public void test_serialization_of_null_String() {
        String nullString = null;
        assertThat("null string", roundTripNullableString(nullString), is(nullString));

        try {
            TestUtil.serializeAndDeserialize(nullString, StringEncoding::writeString, StringEncoding::readNullableString);
            fail("should have thrown NullPointerException on serialization");
        } catch (NullPointerException e) {
            // OK
        }

        try {
            TestUtil.serializeAndDeserialize(nullString, StringEncoding::writeNullableString, StringEncoding::readString);
            fail("should have thrown NullPointerException on deserialization");
        } catch (NullPointerException e) {
            // OK
        }
    }


    private static String roundTripString(String original) {
        return TestUtil.serializeAndDeserialize(original, StringEncoding::writeString, StringEncoding::readString);
    }

    private static String roundTripNullableString(String original) {
        return TestUtil.serializeAndDeserialize(original, StringEncoding::writeNullableString, StringEncoding::readNullableString);
    }
}
