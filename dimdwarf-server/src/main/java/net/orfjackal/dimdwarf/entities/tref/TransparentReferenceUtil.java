/*
 * This file is part of Dimdwarf Application Server <http://dimdwarf.sourceforge.net/>
 *
 * Copyright (c) 2008, Esko Luontola. All Rights Reserved.
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
 *     * Neither the name of the copyright holder nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
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

import net.orfjackal.dimdwarf.api.impl.Entities;
import net.orfjackal.dimdwarf.api.impl.IEntity;
import net.orfjackal.dimdwarf.api.impl.TransparentReference;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Esko Luontola
 * @since 23.1.2008
 */
public class TransparentReferenceUtil {

    private TransparentReferenceUtil() {
    }

    // TODO: marking for update not implemented by entity manager
    public static void markForUpdate(Object object) {
        if (Entities.isTransparentReference(object)) {
//            ((TransparentReference) object).getEntityReference().getForUpdate();
        } else if (Entities.isEntity(object)) {
//            AppContext.getDataManager().markForUpdate(object);
        }
    }

    public static Class<?>[] proxiedInterfaces(Class<?> aClass) {
        List<Class<?>> results = new ArrayList<Class<?>>();
        for (Class<?> c = aClass; c != null; c = c.getSuperclass()) {
            for (Class<?> anInterface : c.getInterfaces()) {
                assert !TransparentReference.class.equals(anInterface);
                if (!IEntity.class.isAssignableFrom(anInterface)) {
                    results.add(anInterface);
                }
            }
        }
        results.add(TransparentReference.class);
        return results.toArray(new Class<?>[results.size()]);
    }

    public static boolean shouldDelegateToTransparentReference(Method method) {
        return method.getDeclaringClass().equals(TransparentReference.class)
                || (method.getDeclaringClass().equals(Object.class) && method.getName().equals("equals"))
                || (method.getDeclaringClass().equals(Object.class) && method.getName().equals("hashCode"));
    }
}
