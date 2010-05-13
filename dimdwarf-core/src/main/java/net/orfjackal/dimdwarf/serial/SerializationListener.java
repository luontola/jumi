// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.serial;

import java.util.EventListener;

public interface SerializationListener extends EventListener {

    void beforeReplace(Object rootObject, Object obj, MetadataBuilder meta);

    void beforeSerialize(Object rootObject, Object obj, MetadataBuilder meta);

    void afterDeserialize(Object obj, MetadataBuilder meta);

    void afterResolve(Object obj, MetadataBuilder meta);
}
