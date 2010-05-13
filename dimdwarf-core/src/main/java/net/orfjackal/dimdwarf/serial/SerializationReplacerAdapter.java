// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.serial;

import javax.annotation.concurrent.Immutable;

@Immutable
public abstract class SerializationReplacerAdapter implements SerializationReplacer {

    public Object replaceSerialized(Object rootObject, Object obj, MetadataBuilder meta) {
        return obj;
    }

    public Object resolveDeserialized(Object obj, MetadataBuilder meta) {
        return obj;
    }
}
