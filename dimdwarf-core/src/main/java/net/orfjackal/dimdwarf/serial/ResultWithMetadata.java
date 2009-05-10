// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.serial;

import net.orfjackal.dimdwarf.util.Objects;

import javax.annotation.concurrent.Immutable;
import java.util.*;

/**
 * @author Esko Luontola
 * @since 11.12.2008
 */
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
