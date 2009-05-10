// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.serial;

import java.util.EventListener;

/**
 * @author Esko Luontola
 * @since 1.9.2008
 */
public interface SerializationListener extends EventListener {

    void beforeReplace(Object rootObject, Object obj, MetadataBuilder meta);

    void beforeSerialize(Object rootObject, Object obj, MetadataBuilder meta);

    void afterDeserialize(Object obj, MetadataBuilder meta);

    void afterResolve(Object obj, MetadataBuilder meta);
}
