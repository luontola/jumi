// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.serial;

import net.orfjackal.dimdwarf.db.Blob;

import javax.annotation.concurrent.Immutable;
import java.util.*;

@Immutable
public class SerializationResult extends ResultWithMetadata {

    private final Blob serializedBytes;

    public SerializationResult(Blob serializedBytes, Map<Class<?>, List<?>> metadata) {
        super(metadata);
        this.serializedBytes = serializedBytes;
    }

    public Blob getSerializedBytes() {
        return serializedBytes;
    }
}
