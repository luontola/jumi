// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.scheduler;

import javax.annotation.Nullable;
import java.util.Collection;

public interface RecoverableSet<T> {

    String SEPARATOR = ":";

    String put(T value);

    @Nullable
    T remove(String key);

    @Nullable
    T get(String key);

    Collection<T> getAll();
}
