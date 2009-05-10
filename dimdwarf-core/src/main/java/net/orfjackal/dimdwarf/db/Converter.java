// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db;

import javax.annotation.Nullable;


/**
 * @author Esko Luontola
 * @since 12.9.2008
 */
public interface Converter<T, U> {

    @Nullable
    T back(@Nullable U value);

    @Nullable
    U forth(@Nullable T value);
}
