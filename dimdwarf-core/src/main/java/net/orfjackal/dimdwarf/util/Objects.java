// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util;

import javax.annotation.Nullable;

public class Objects {

    private Objects() {
    }

    /**
     * Helps to avoid using {@code @SuppressWarnings({"unchecked"})} when casting to a generic type.
     */
    @Nullable
    @SuppressWarnings({"unchecked"})
    public static <T> T uncheckedCast(@Nullable Object obj) {
        return (T) obj;
    }

    public static boolean safeEquals(@Nullable Object x, @Nullable Object y) {
        return x == y || (x != null && x.equals(y));
    }
}
