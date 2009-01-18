/*
 * This file is part of Dimdwarf Application Server <http://dimdwarf.sourceforge.net/>
 *
 * Copyright (c) 2008-2009, Esko Luontola. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
            return ((TransparentReference) obj).getEntityReference();
        } else if (Entities.isEntity(obj)) {
            return ThreadContext.get(ReferenceFactory.class).createReference(obj);
        } else {
            return null;
        }
    }
}
