// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.api.internal;

import net.orfjackal.dimdwarf.api.EntityId;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * @author Esko Luontola
 * @since 13.8.2009
 */
public class ObjectId implements EntityId, Serializable {
    private static final long serialVersionUID = 1L;

    private final long id;

    public ObjectId(long id) {
        this.id = id;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ObjectId)) {
            return false;
        }
        ObjectId that = (ObjectId) obj;
        return this.id == that.id;
    }

    public int hashCode() {
        return (int) id;
    }

    public BigInteger toBigInteger() {
        return BigInteger.valueOf(id);
    }

    public String toString() {
        return getClass().getSimpleName() + "(" + id + ")";
    }
}
