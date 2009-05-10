// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.serial;

import javax.annotation.concurrent.Immutable;

/**
 * @author Esko Luontola
 * @since 1.9.2008
 */
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
