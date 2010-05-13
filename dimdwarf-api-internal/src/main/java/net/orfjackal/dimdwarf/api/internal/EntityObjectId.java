// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.api.internal;

import net.orfjackal.dimdwarf.api.EntityId;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * Unique ID for an entity of type {@link EntityObject}. Later on there might be added
 * more entities, of which some are stored in the database in a different format than
 * the entity objects of application code. Examples of such are: sessions, channels, tasks.
 * These will probably be stored in their own database, separate from application entities.
 * <p/>
 * TODO: When that happens, there will be need to be more careful where EntityId and where EntityObjectId is used.
 * It will be necessary to make a distinction between different types of EntityIds in the
 * implementation level, but to the application programmer the IDs should not be visible.
 */
public class EntityObjectId implements EntityId, Serializable {
    private static final long serialVersionUID = 1L;

    private final long id;

    public EntityObjectId(long id) {
        this.id = id;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof EntityObjectId)) {
            return false;
        }
        EntityObjectId that = (EntityObjectId) obj;
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
