// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities.tref;


import net.orfjackal.dimdwarf.api.internal.*;

import javax.annotation.concurrent.Immutable;
import java.io.Serializable;

/**
 * @author Esko Luontola
 * @since 31.1.2008
 */
@Immutable
public class TransparentReferenceImpl implements TransparentReference, Serializable {
    private static final long serialVersionUID = 1L;

    private final EntityReference<?> reference;
    private final Class<?> type;

    public TransparentReferenceImpl(Class<?> type, EntityReference<?> reference) {
        this.type = type;
        this.reference = reference;
    }

    public Object getEntity() {
        return reference.get();
    }

    public EntityReference<?> getEntityReference() {
        return reference;
    }

    public Class<?> getType() {
        return type;
    }

    public boolean equals(Object obj) {
        return EntityHelper.equals(this, obj);
    }

    public int hashCode() {
        return EntityHelper.hashCode(this);
    }

    public Object writeReplace() {
        return this;
    }
}
