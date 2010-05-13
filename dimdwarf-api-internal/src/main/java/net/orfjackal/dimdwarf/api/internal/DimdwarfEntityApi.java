// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.api.internal;

public class DimdwarfEntityApi implements EntityApi {

    public boolean isEntity(Object obj) {
        return obj instanceof EntityObject && !isTransparentReference(obj);
    }

    public boolean isTransparentReference(Object obj) {
        return obj instanceof TransparentReference;
    }
}
