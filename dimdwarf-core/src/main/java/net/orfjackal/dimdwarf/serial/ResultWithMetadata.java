// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.serial;

import net.orfjackal.dimdwarf.util.Objects;

import javax.annotation.concurrent.Immutable;
import java.util.*;

@Immutable
public abstract class ResultWithMetadata {

    private final Map<Class<?>, List<?>> metadata;

    public ResultWithMetadata(Map<Class<?>, List<?>> metadata) {
        this.metadata = metadata;
    }

    public <T> List<T> getMetadata(Class<?> key) {
        List<?> list = metadata.get(key);
        if (list == null) {
            list = Collections.emptyList();
        }
        return Objects.uncheckedCast(Collections.unmodifiableList(list));
    }
}
