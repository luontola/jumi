// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

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
