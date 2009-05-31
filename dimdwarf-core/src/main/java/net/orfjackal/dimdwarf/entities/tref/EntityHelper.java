// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities.tref;

import net.orfjackal.dimdwarf.api.internal.*;
import net.orfjackal.dimdwarf.context.ThreadContext;
import net.orfjackal.dimdwarf.entities.ReferenceFactory;
import net.orfjackal.dimdwarf.util.Objects;

import javax.annotation.Nullable;

/**
 * For transparent references to work correctly, all subclasses of {@link EntityObject} should
 * define their {@link #equals(Object)} and {@link #hashCode()} methods as follows:
 * <pre><code>
 * public boolean equals(Object obj) {
 *     return EntityHelper.equals(this, obj);
 * }
 * public int hashCode() {
 *     return EntityHelper.hashCode(this);
 * }
 * </code></pre>
 *
 * @author Esko Luontola
 * @since 1.2.2008
 */
public class EntityHelper {

    private EntityHelper() {
    }

    public static boolean equals(@Nullable Object obj1, @Nullable Object obj2) {
        Object id1 = getReference(obj1);
        Object id2 = getReference(obj2);
        return Objects.safeEquals(id1, id2);
    }

    public static int hashCode(@Nullable Object obj) {
        Object id = getReference(obj);
        return id.hashCode();
    }

    @Nullable
    private static EntityReference<?> getReference(@Nullable Object obj) {
        if (Entities.isTransparentReference(obj)) {
            return ((TransparentReference) obj).getEntityReference$TREF();
        } else if (Entities.isEntity(obj)) {
            return ThreadContext.get(ReferenceFactory.class).createReference(obj);
        } else {
            return null;
        }
    }
}
