// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.api.internal;

public class DimdwarfEntityApi implements EntityApi {

    public boolean isEntity(Object obj) {
        return obj instanceof EntityObject && !isTransparentReference(obj);
    }

    public boolean isTransparentReference(Object obj) {
        return obj instanceof TransparentReference;
    }
}
