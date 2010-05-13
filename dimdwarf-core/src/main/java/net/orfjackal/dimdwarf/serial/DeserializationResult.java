// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.serial;

import javax.annotation.concurrent.Immutable;
import java.util.*;

@Immutable
public class DeserializationResult extends ResultWithMetadata {

    private final Object deserializedObject;

    public DeserializationResult(Object deserializedObject, Map<Class<?>, List<?>> metadata) {
        super(metadata);
        this.deserializedObject = deserializedObject;
    }

    public Object getDeserializedObject() {
        return deserializedObject;
    }
}
