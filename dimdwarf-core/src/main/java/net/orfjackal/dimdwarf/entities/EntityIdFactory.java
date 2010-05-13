// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import com.google.inject.*;
import net.orfjackal.dimdwarf.api.EntityId;
import net.orfjackal.dimdwarf.api.internal.EntityObjectId;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Esko Luontola
 * @since 4.9.2008
 */
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
