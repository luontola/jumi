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

package net.orfjackal.dimdwarf.scopes;

import com.google.inject.*;
import net.orfjackal.dimdwarf.context.Context;
import net.orfjackal.dimdwarf.context.*;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;

/**
 * When {@code TaskScopedContext} is installed as the current {@link ThreadContext},
 * then that thread is in a task scope identified by the {@code TaskScopedContext} instance.
 *
 * @author Esko Luontola
 * @since 13.9.2008
 */
@NotThreadSafe
public class TaskScopedContext implements Context {

    private final Map<Key<?>, Object> cache = new HashMap<Key<?>, Object>();
    private final Injector injector;

    @Inject
    public TaskScopedContext(Injector injector) {
        this.injector = injector;
    }

    public <T> T get(Class<T> service) {
        return injector.getInstance(service);
    }

    <T> T scopedGet(Key<T> key, Provider<T> unscoped) {
        T value = (T) cache.get(key);
        if (value == null) {
            value = unscoped.get();
            cache.put(key, value);
        }
        return value;
    }
}
