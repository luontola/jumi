// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.serial;

import java.util.EventListener;

public interface SerializationReplacer extends EventListener {

    Object replaceSerialized(Object rootObject, Object obj, MetadataBuilder meta);

    Object resolveDeserialized(Object obj, MetadataBuilder meta);
}
