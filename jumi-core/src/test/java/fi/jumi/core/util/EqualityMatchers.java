// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.util;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.hamcrest.*;

import java.lang.reflect.*;
import java.util.List;

public class EqualityMatchers {

    public static <T> Matcher<T> deepEqualTo(T expected) {
        return new TypeSafeMatcher<T>() {
            @Override
            protected boolean matchesSafely(T actual) {
                return findDifference("this", actual, expected) == null;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("deep equal to ").appendValue(expected);
            }

            @Override
            protected void describeMismatchSafely(T actual, Description mismatchDescription) {
                String path = findDifference("this", actual, expected);
                mismatchDescription.appendText("was different at ").appendValue(path)
                        .appendText(" of ").appendValue(actual);
            }
        };
    }

    private static String findDifference(String path, Object obj1, Object obj2) {
        if (obj1 == null || obj2 == null) {
            return obj1 == obj2 ? null : path;
        }
        if (obj1.getClass() != obj2.getClass()) {
            return path;
        }

        // collections have a custom equals, but we want deep equality on every collection element
        // TODO: support other collection types? comparing Sets should be order-independent
        if (obj1 instanceof List) {
            List<?> col1 = (List<?>) obj1;
            List<?> col2 = (List<?>) obj2;

            int size1 = col1.size();
            int size2 = col2.size();
            if (size1 != size2) {
                return path + ".size()";
            }

            for (int i = 0; i < Math.min(size1, size2); i++) {
                String diff = findDifference(path + ".get(" + i + ")", col1.get(i), col2.get(i));
                if (diff != null) {
                    return diff;
                }
            }
            return null;
        }

        // use custom equals method if exists
        try {
            Method equals = obj1.getClass().getMethod("equals", Object.class);
            if (equals.getDeclaringClass() != Object.class) {
                return obj1.equals(obj2) ? null : path;
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        // arrays
        if (obj2.getClass().isArray()) {
            int length1 = Array.getLength(obj1);
            int length2 = Array.getLength(obj2);
            if (length1 != length2) {
                return path + ".length";
            }

            for (int i = 0; i < Math.min(length1, length2); i++) {
                String diff = findDifference(path + "[" + i + "]",
                        Array.get(obj1, i),
                        Array.get(obj2, i));
                if (diff != null) {
                    return diff;
                }
            }
            return null;
        }

        // structural equality
        for (Class<?> cl = obj2.getClass(); cl != null; cl = cl.getSuperclass()) {
            for (Field field : cl.getDeclaredFields()) {
                try {
                    String diff = findDifference(path + "." + field.getName(),
                            FieldUtils.readField(field, obj1, true),
                            FieldUtils.readField(field, obj2, true));
                    if (diff != null) {
                        return diff;
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }
}
