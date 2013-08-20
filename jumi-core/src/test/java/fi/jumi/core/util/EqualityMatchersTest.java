// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.util;

import org.hamcrest.StringDescription;
import org.junit.Test;

import static fi.jumi.core.util.EqualityMatchers.deepEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class EqualityMatchersTest {

    @Test
    public void basic_types() {
        assertThat("foo", deepEqualTo("foo"));
        assertThat("foo", not(deepEqualTo("bar")));
        assertThat(1, deepEqualTo(1));
        assertThat(true, deepEqualTo(true));
        assertThat("foo", not(deepEqualTo(null)));
        assertThat(null, not(deepEqualTo("foo")));
    }

    @Test
    public void arrays() {
        assertThat("same", new Object[]{"foo", 1}, deepEqualTo(new Object[]{"foo", 1}));
        assertThat("longer", new Object[]{"foo", 1, 2}, not(deepEqualTo(new Object[]{"foo", 1})));
        assertThat("shorter", new Object[]{"foo"}, not(deepEqualTo(new Object[]{"foo", 1})));
        assertThat("different order", new Object[]{1, "foo"}, not(deepEqualTo(new Object[]{"foo", 1})));
        assertThat("different value", new Object[]{"foo", 2}, not(deepEqualTo(new Object[]{"foo", 1})));
        assertThat("same primitive array", new int[]{1, 2}, deepEqualTo(new int[]{1, 2}));
        assertThat("different primitive array", new int[]{1, 3}, not(deepEqualTo(new int[]{1, 2})));
    }

    @Test
    public void classes_that_do_not_implement_equals() {
        assertThat("same", new NoEqualsDummy("foo"), deepEqualTo(new NoEqualsDummy("foo")));
        assertThat("different", new NoEqualsDummy("foo"), not(deepEqualTo(new NoEqualsDummy("bar"))));

        assertThat("same nested", new NoEqualsDummy(new NoEqualsDummy("foo")), deepEqualTo(new NoEqualsDummy(new NoEqualsDummy("foo"))));
        assertThat("different nested", new NoEqualsDummy(new NoEqualsDummy("foo")), not(deepEqualTo(new NoEqualsDummy(new NoEqualsDummy("bar")))));

        assertThat("array of same", new Object[]{new NoEqualsDummy("foo")}, deepEqualTo(new Object[]{new NoEqualsDummy("foo")}));
        assertThat("array of different", new Object[]{new NoEqualsDummy("foo")}, not(deepEqualTo(new Object[]{new NoEqualsDummy("bar")})));
    }

    @Test
    public void failure_description_says_what_field_was_different() {
        assertThat(
                getMismatchDescription(
                        new NoEqualsDummy(new Object[]{new NoEqualsDummy("foo")}),
                        new NoEqualsDummy(new Object[]{new NoEqualsDummy("bar")})),
                containsString("\"this.obj[0].obj\""));
        assertThat(
                getMismatchDescription(
                        new NoEqualsDummy(new Object[]{new NoEqualsDummy("foo")}),
                        new NoEqualsDummy(new Object[]{null, null})),
                containsString("\"this.obj.length\""));
    }

    private static String getMismatchDescription(Object actual, Object expected) {
        StringDescription description = new StringDescription();
        deepEqualTo(expected).describeMismatch(actual, description);
        return description.toString();
    }

    private static class NoEqualsDummy {
        private final Object obj;

        private NoEqualsDummy(Object obj) {
            this.obj = obj;
        }
    }
}
