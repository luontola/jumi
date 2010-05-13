// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.serial;

import net.orfjackal.dimdwarf.util.Objects;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;

@NotThreadSafe
public class MetadataBuilderImpl implements MetadataBuilder {

    // TODO: try to get rid of MetadataBuilder, because it complicates the serialization process even for listeners which do not use it 

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
