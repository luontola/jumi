// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.scheduler;

import net.orfjackal.dimdwarf.api.*;
import net.orfjackal.dimdwarf.entities.*;
import net.orfjackal.dimdwarf.util.Objects;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Provider;
import java.util.*;

@ThreadSafe
public class RecoverableSetImpl<T> implements RecoverableSet<T> {

    // FIXME: RecoverableSetImpl will be removed/refactored in new architecture

    private final String prefix;
    private final Provider<BindingRepository> bindings;
    private final Provider<EntityInfo> info;

    public RecoverableSetImpl(String prefix, Provider<BindingRepository> bindings, Provider<EntityInfo> info) {
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
        EntityId id = info.get().getEntityId(value);
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
