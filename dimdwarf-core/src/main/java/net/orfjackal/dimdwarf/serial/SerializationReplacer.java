// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.serial;

import java.util.EventListener;

public interface SerializationReplacer extends EventListener {

    Object replaceSerialized(Object rootObject, Object obj, MetadataBuilder meta);

    Object resolveDeserialized(Object obj, MetadataBuilder meta);
}
