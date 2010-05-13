// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db;

import javax.annotation.concurrent.Immutable;

@Immutable
public class NoConversion<T> implements Converter<T, T> {

    public T back(T value) {
        return value;
    }

    public T forth(T value) {
        return value;
    }
}
