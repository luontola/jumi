// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import com.google.inject.*;
import net.orfjackal.dimdwarf.api.EntityId;
import net.orfjackal.dimdwarf.api.internal.EntityObjectId;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
@ThreadSafe
public class EntityIdFactory {

    private final AtomicLong counter = new AtomicLong();

    @Inject
    public EntityIdFactory(@MaxEntityId long largestUsedId) {
        counter.set(largestUsedId);
    }

    public EntityId newId() {
        long nextId = counter.incrementAndGet();
        return new EntityObjectId(nextId);
    }
}
