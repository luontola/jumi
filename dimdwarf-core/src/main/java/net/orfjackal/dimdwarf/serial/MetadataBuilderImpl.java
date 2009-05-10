// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.serial;

import net.orfjackal.dimdwarf.util.Objects;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;

/**
 * @author Esko Luontola
 * @since 28.12.2008
 */
@NotThreadSafe
public class MetadataBuilderImpl implements MetadataBuilder {

    private final HashMap<Class<?>, List<Object>> metadata = new HashMap<Class<?>, List<Object>>();

    public void append(Class<?> key, Object value) {
        List<Object> list = metadata.get(key);
        if (list == null) {
            list = new ArrayList<Object>();
            metadata.put(key, list);
        }
        list.add(value);
    }

    public Map<Class<?>, List<?>> getMetadata() {
        return Objects.uncheckedCast(metadata);
    }
}
