// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.api.internal;

/**
 * @author Esko Luontola
 * @since 10.9.2008
 */
public class DimdwarfEntityApi implements EntityApi {

    public boolean isEntity(Object obj) {
        return obj instanceof EntityObject && !isTransparentReference(obj);
    }

    public boolean isTransparentReference(Object obj) {
        return obj instanceof TransparentReference;
    }
}
