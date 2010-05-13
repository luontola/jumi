// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db;

import javax.annotation.Nullable;

public interface Converter<T, U> {

    @Nullable
    T back(@Nullable U value);

    @Nullable
    U forth(@Nullable T value);
}
