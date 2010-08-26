// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util;

import com.google.inject.Provider;

public class StubProvider<T> implements Provider<T> {

    private final T instance;

    public StubProvider(T instance) {
        this.instance = instance;
    }

    public T get() {
        return instance;
    }

    public static <T> Provider<T> providerOf(T instance) {
        return new StubProvider<T>(instance);
    }
}
