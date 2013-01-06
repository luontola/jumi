// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.api;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class TestFileTest {

    @Test
    @SuppressWarnings("ObjectEqualsNull")
    public void is_a_value_object() {
        TestFile a1 = TestFile.fromClassName("A");
        TestFile a2 = TestFile.fromClassName("A");
        TestFile b = TestFile.fromClassName("B");

        assertTrue("equals: same object", a1.equals(a1));
        assertTrue("equals: same value", a1.equals(a2));
        assertFalse("equals: different value", a1.equals(b));
        assertFalse("equals: null", a1.equals(null));

        assertTrue("hashCode: same value", a1.hashCode() == a2.hashCode());
        assertFalse("hashCode: different value", a1.hashCode() == b.hashCode());
    }

    @Test
    public void to_string() {
        assertThat("toString: class file", TestFile.fromClassName("com.example.Foo").toString(), is("com.example.Foo"));
        // TODO: non-class file
    }
}
