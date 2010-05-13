// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import net.orfjackal.dimdwarf.serial.*;

import javax.annotation.concurrent.Immutable;

@Immutable
public class CheckInnerClassSerialized extends SerializationAdapter {

    @Override
    public void beforeSerialize(Object rootObject, Object obj, MetadataBuilder meta) {
        // Serializing anonymous and local classes is dangerous, because their class names are generated
        // automatically ($1, $2, $1Local, $2Local etc.), which means that the next time that such an
        // object is deserialized from database, the class name might have changed because of
        // a change to the enclosing class, and the system might end up in an unspecified state.
        // Also "Java Object Serialization Specification 6.0" strongly discourages serialization of
        // inner classes (http://java.sun.com/javase/6/docs/platform/serialization/spec/serial-arch.html#7182).
        // It is safer to serialize only top-level classes and member classes.
        Class<?> cls = obj.getClass();
        if (cls.isAnonymousClass()) {
            throw new IllegalArgumentException("Tried to serialize an anonymous class: " + cls.getName());
        }
        if (cls.isLocalClass()) {
            throw new IllegalArgumentException("Tried to serialize a local class: " + cls.getName());
        }
    }
}
