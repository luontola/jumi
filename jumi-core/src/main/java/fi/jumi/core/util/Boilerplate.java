// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.util;

import com.google.common.base.Throwables;

import javax.annotation.concurrent.Immutable;

@Immutable
public class Boilerplate {

    public static RuntimeException rethrow(Throwable t) {
        return Throwables.propagate(t);
    }

    public static String toString(Class<?> clazz, Object... fields) {
        StringBuilder sb = new StringBuilder();
        sb.append(nameWithoutPackage(clazz));
        sb.append('(');
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(fields[i]);
        }
        sb.append(')');
        return sb.toString();
    }

    private static String nameWithoutPackage(Class<?> clazz) {
        // We're not using Class.getSimpleName() because it does not show
        // the name of the enclosing class of inner classes.
        int prefixLength = clazz.getPackage().getName().length() + 1;
        return clazz.getName().substring(prefixLength);
    }
}
