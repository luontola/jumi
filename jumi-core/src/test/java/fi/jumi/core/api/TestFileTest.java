// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.api;

import org.junit.*;
import org.junit.rules.ExpectedException;

import java.nio.file.Paths;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class TestFileTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

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
        assertThat("toString: non-class file", TestFile.fromPath(Paths.get("com", "example", "Foo.test")).toString(), is("com/example/Foo.test"));
    }

    @Test
    public void properties_of_class_files() {
        TestFile testFile = TestFile.fromPath(Paths.get("foo", "bar", "SomeTest.class"));

        assertThat("getPath", testFile.getPath(), is("foo/bar/SomeTest.class"));
        assertThat("isClass", testFile.isClass(), is(true));
        assertThat("getClassName", testFile.getClassName(), is("foo.bar.SomeTest"));
    }

    @Test
    public void properties_of_non_class_files() {
        TestFile testFile = TestFile.fromPath(Paths.get("foo", "bar", "SomeTest.txt"));

        assertThat("getPath", testFile.getPath(), is("foo/bar/SomeTest.txt"));
        assertThat("isClass", testFile.isClass(), is(false));

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("not a class file: foo/bar/SomeTest.txt");
        testFile.getClassName();
    }
}
