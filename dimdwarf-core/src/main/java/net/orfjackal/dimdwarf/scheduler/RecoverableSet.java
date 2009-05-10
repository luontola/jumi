// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.scheduler;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * @author Esko Luontola
 * @since 26.11.2008
 */
public interface RecoverableSet<T> {

    String SEPARATOR = ":";

    String put(T value);

    @Nullable
    T remove(String key);

    @Nullable
    T get(String key);

    Collection<T> getAll();
}
