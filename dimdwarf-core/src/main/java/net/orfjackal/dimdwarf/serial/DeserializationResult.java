// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

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
