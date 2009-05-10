// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.serial;

import javax.annotation.concurrent.Immutable;

/**
 * @author Esko Luontola
 * @since 4.9.2008
 */
@Immutable
public abstract class SerializationReplacerAdapter implements SerializationReplacer {

    public Object replaceSerialized(Object rootObject, Object obj, MetadataBuilder meta) {
        return obj;
    }

    public Object resolveDeserialized(Object obj, MetadataBuilder meta) {
        return obj;
    }
}
