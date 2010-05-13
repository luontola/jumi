// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

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
