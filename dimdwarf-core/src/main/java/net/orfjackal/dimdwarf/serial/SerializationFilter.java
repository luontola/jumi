// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.serial;

public interface SerializationFilter {

    Object replaceSerialized(Object rootObject, Object obj);

    Object resolveDeserialized(Object obj);
}
