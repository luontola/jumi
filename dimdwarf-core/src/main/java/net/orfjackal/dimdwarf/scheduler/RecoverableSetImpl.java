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

package net.orfjackal.dimdwarf.scheduler;

import com.google.inject.Provider;
import net.orfjackal.dimdwarf.api.EntityInfo;
import net.orfjackal.dimdwarf.entities.*;
import net.orfjackal.dimdwarf.util.Objects;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.math.BigInteger;
import java.util.*;

/**
 * @author Esko Luontola
 * @since 26.11.2008
 */
@ThreadSafe
public class RecoverableSetImpl<T> implements RecoverableSet<T> {

    private final String prefix;
    private final Provider<BindingStorage> bindings;
    private final Provider<EntityInfo> info;

    public RecoverableSetImpl(String prefix, Provider<BindingStorage> bindings, Provider<EntityInfo> info) {
        this.prefix = prefix + SEPARATOR;
        this.bindings = bindings;
        this.info = info;
    }

    public String put(T value) {
        String key = keyFor(value);
        bindings.get().update(key, value);
        return key;
    }

    private String keyFor(T value) {
        BigInteger id = info.get().getEntityId(value);
        return prefix + id;
    }

    @Nullable
    public T remove(String key) {
        T value = get(key);
        bindings.get().delete(key);
        return value;
    }

    @Nullable
    public T get(String key) {
        checkKeyHasRightPrefix(key);
        return Objects.<T>uncheckedCast(bindings.get().read(key));
    }

    private void checkKeyHasRightPrefix(String key) {
        if (!key.startsWith(prefix)) {
            throw new IllegalArgumentException("The key " + key + " is not prefixed " + prefix);
        }
    }

    public Collection<T> getAll() {
        List<T> result = new ArrayList<T>();
        for (String key : new BindingWalker(prefix, bindings.get())) {
            Object value = get(key);
            if (value != null) {
                result.add(Objects.<T>uncheckedCast(value));
            }
        }
        return Collections.unmodifiableCollection(result);
    }
}
