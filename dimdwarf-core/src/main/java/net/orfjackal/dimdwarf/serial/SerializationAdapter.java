// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.serial;

import javax.annotation.concurrent.Immutable;

@Immutable
public abstract class SerializationAdapter implements SerializationListener {

    public void beforeReplace(Object rootObject, Object obj, MetadataBuilder meta) {
    }

    public void beforeSerialize(Object rootObject, Object obj, MetadataBuilder meta) {
    }

    public void afterDeserialize(Object obj, MetadataBuilder meta) {
    }

    public void afterResolve(Object obj, MetadataBuilder meta) {
    }
}
