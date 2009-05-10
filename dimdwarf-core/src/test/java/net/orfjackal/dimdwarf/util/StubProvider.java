// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util;

import com.google.inject.Provider;

/**
 * @author Esko Luontola
 * @since 13.9.2008
 */
public class StubProvider<T> implements Provider<T> {

    private final T instance;

    public StubProvider(T instance) {
        this.instance = instance;
    }

    public T get() {
        return instance;
    }

    public static <T> Provider<T> wrap(T instance) {
        return new StubProvider<T>(instance);
    }
}
