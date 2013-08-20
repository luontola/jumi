// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.util;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.hamcrest.*;

import java.lang.reflect.*;

public class EqualityMatchers {


    public static <T> Matcher<T> deepEqualTo(T expected) {
        return new TypeSafeMatcher<T>() {
            @Override
            protected boolean matchesSafely(T actual) {
                return areDeepEqual(actual, expected);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("structurally equal to ").appendValue(expected);
            }

//            @Override
//            protected void describeMismatchSafely(T actual, Description mismatchDescription) {
//                boolean path = areDeepEqual(actual, expected);
//                mismatchDescription.appendText("was different at ").appendValue(path)
//                        .appendText(" of ").appendValue(actual);
//            }
        };
    }

    private static boolean areDeepEqual(Object actual, Object expected) {
        if (actual.getClass() != expected.getClass()) {
            return false;
        }

        // use custom equals method if exists
        try {
            Method equals = actual.getClass().getMethod("equals", Object.class);
            if (equals.getDeclaringClass() != Object.class) {
                return actual.equals(expected);
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        // fall back to structural equality
        EqualsBuilder eq = new EqualsBuilder();
        if (expected.getClass().isArray()) {
            int actualLength = Array.getLength(actual);
            int expectedLength = Array.getLength(expected);
            eq.append(actualLength, expectedLength);
            for (int i = 0; i < Math.min(actualLength, expectedLength); i++) {
                eq.append(areDeepEqual(Array.get(actual, i), Array.get(expected, i)), true);
            }
        } else {
            for (Class<?> cl = expected.getClass(); cl != null; cl = cl.getSuperclass()) {
                for (Field field : cl.getDeclaredFields()) {
                    try {
                        eq.append(areDeepEqual(FieldUtils.readField(field, actual, true), FieldUtils.readField(field, expected, true)), true);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return eq.build();
    }
}
