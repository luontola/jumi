// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.serial;

public class NullSerializationFilter implements SerializationFilter {

    public Object replaceSerialized(Object rootObject, Object obj) {
        return obj;
    }

    public Object resolveDeserialized(Object obj) {
        return obj;
    }
}
